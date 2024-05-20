/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.flow.controller;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * 节流控制器-排队等待
 * <image src="../../../../../../../../../../../../images/排队等待.png"/>
 * @author Eric Zhao
 * @author jialiang.linjl
 * @since 2.0
 */
public class ThrottlingController implements TrafficShapingController {

    // Refactored from legacy RateLimitController of Sentinel 1.x.
    // 从Sentinel 1.x的遗留RateLimitController重构
    private static final long MS_TO_NS_OFFSET = TimeUnit.MILLISECONDS.toNanos(1);
    // 超时时间
    private final int maxQueueingTimeMs;
    // 统计持续时间（毫秒）
    private final int statDurationMs;
    // 阈值
    private final double count;
    // 是否使用纳秒  如果阈值{count}大于1000，则使用纳秒
    private final boolean useNanoSeconds;
    // 最近一次请求通过的时间
    private final AtomicLong latestPassedTime = new AtomicLong(-1);

    public ThrottlingController(int queueingTimeoutMs, double maxCountPerStat) {
        this(queueingTimeoutMs, maxCountPerStat, 1000);
    }

    public ThrottlingController(int queueingTimeoutMs, double maxCountPerStat, int statDurationMs) {
        AssertUtil.assertTrue(statDurationMs > 0, "statDurationMs should be positive");
        AssertUtil.assertTrue(maxCountPerStat >= 0, "maxCountPerStat should be >= 0");
        AssertUtil.assertTrue(queueingTimeoutMs >= 0, "queueingTimeoutMs should be >= 0");
        this.maxQueueingTimeMs = queueingTimeoutMs;
        this.count = maxCountPerStat;
        this.statDurationMs = statDurationMs;
        // Use nanoSeconds when durationMs%count != 0 or count/durationMs> 1 (to be accurate)
        if (maxCountPerStat > 0) {
            // 如果阈值大于1000，则使用纳秒
            this.useNanoSeconds = statDurationMs % Math.round(maxCountPerStat) != 0 || maxCountPerStat / statDurationMs > 1;
        } else {
            this.useNanoSeconds = false;
        }
    }

    @Override
    public boolean canPass(Node node, int acquireCount) {
        return canPass(node, acquireCount, false);
    }

    /**
     * 检测是否可以通过流控（纳秒）
     * @param acquireCount      获取的资源数
     * @param maxCountPerStat   流控资源阈值
     * @return
     */
    private boolean checkPassUsingNanoSeconds(int acquireCount, double maxCountPerStat) {
        // 计算出超时的纳秒值
        final long maxQueueingTimeNs = maxQueueingTimeMs * MS_TO_NS_OFFSET;
        long currentTime = System.nanoTime();
        // Calculate the interval between every two requests.
        // 计算每两个请求之间的间隔（比如我1毫秒需要1个请求， 我的阈值为10，那么的一个请求时长为0.1毫秒）
        final long costTimeNs = Math.round(1.0d * MS_TO_NS_OFFSET * statDurationMs * acquireCount / maxCountPerStat);

        // Expected pass time of this request.
        // 此请求的预期通过时间（如果当前的纳秒时间为1000, 上一次通过的纳秒时间为900, 那么我这次预计请求通过时间为10纳秒）
        long expectedTime = costTimeNs + latestPassedTime.get();

        if (expectedTime <= currentTime) {
            // Contention may exist here, but it's okay.
            // 放入当前时间，作为最近通过的请求时间
            latestPassedTime.set(currentTime);
            return true;
        } else {
            final long curNanos = System.nanoTime();
            // 计算出等待时间
            long waitTime = costTimeNs + latestPassedTime.get() - curNanos;
            // 大于超时时间则直接不通过
            if (waitTime > maxQueueingTimeNs) {
                return false;
            }
            // 重新计算
            long oldTime = latestPassedTime.addAndGet(costTimeNs);
            waitTime = oldTime - curNanos;
            // 如果超过等待时间，则需要减去等待的时间（恢复到上次请求通过的时间）
            if (waitTime > maxQueueingTimeNs) {
                latestPassedTime.addAndGet(-costTimeNs);
                return false;
            }
            // in race condition waitTime may <= 0
            if (waitTime > 0) {
                sleepNanos(waitTime);
            }
            return true;
        }
    }

    /**
     * 检测是否可以通过流控（毫秒）
     * @param acquireCount      获取的资源数
     * @param maxCountPerStat   流控资源阈值
     * @return
     */
    private boolean checkPassUsingCachedMs(int acquireCount, double maxCountPerStat) {
        long currentTime = TimeUtil.currentTimeMillis();
        // Calculate the interval between every two requests.
        long costTime = Math.round(1.0d * statDurationMs * acquireCount / maxCountPerStat);

        // Expected pass time of this request.
        long expectedTime = costTime + latestPassedTime.get();

        if (expectedTime <= currentTime) {
            // Contention may exist here, but it's okay.
            latestPassedTime.set(currentTime);
            return true;
        } else {
            // Calculate the time to wait.
            long waitTime = costTime + latestPassedTime.get() - TimeUtil.currentTimeMillis();
            if (waitTime > maxQueueingTimeMs) {
                return false;
            }

            long oldTime = latestPassedTime.addAndGet(costTime);
            waitTime = oldTime - TimeUtil.currentTimeMillis();
            if (waitTime > maxQueueingTimeMs) {
                latestPassedTime.addAndGet(-costTime);
                return false;
            }
            // in race condition waitTime may <= 0
            if (waitTime > 0) {
                sleepMs(waitTime);
            }
            return true;
        }
    }

    @Override
    public boolean canPass(Node node, int acquireCount, boolean prioritized) {
        // Pass when acquire count is less or equal than 0.
        // 当获取计数小于或等于0时通过
        if (acquireCount <= 0) {
            return true;
        }
        // Reject when count is less or equal than 0.
        // Otherwise, the costTime will be max of long and waitTime will overflow in some cases.
        // 当count小于或等于0时拒绝。 否则，在某些情况下，costTime将是长时间的最大值，waitTime将溢出
        if (count <= 0) {
            return false;
        }
        if (useNanoSeconds) {
            return checkPassUsingNanoSeconds(acquireCount, this.count);
        } else {
            return checkPassUsingCachedMs(acquireCount, this.count);
        }
    }

    private void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    private void sleepNanos(long ns) {
        LockSupport.parkNanos(ns);
    }

}
