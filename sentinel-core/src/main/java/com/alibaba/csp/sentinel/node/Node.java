/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.node;

import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.metric.DebugSupport;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 * Holds real-time statistics for resources.
 * 保存资源的实时统计数据
 * @author qinan.qn
 * @author leyou
 * @author Eric Zhao
 */
public interface Node extends OccupySupport, DebugSupport {

    /**
     * Get incoming request per minute ({@code pass + block}).
     * 每分钟获取传入请求（ pass + block ）。
     * @return total request count per minute
     */
    long totalRequest();

    /**
     * Get pass count per minute.
     * 获取每分钟的通过次数。
     * @return total passed request count per minute
     * @since 1.5.0
     */
    long totalPass();

    /**
     * Get {@link Entry#exit()} count per minute.
     * 获取每分钟Entry.exit()计数。（每分钟完成的请求总数）
     * 返回：
     * @return total completed request count per minute
     */
    long totalSuccess();

    /**
     * Get blocked request count per minute (totalBlockRequest).
     * 获取每分钟阻止的请求计数 (totalBlockRequest)。
     * @return total blocked request count per minute
     */
    long blockRequest();

    /**
     * Get exception count per minute.
     * 获取每分钟的异常计数
     * @return total business exception count per minute
     */
    long totalException();

    /**
     * Get pass request per second.
     * 每秒获取通行证请求。
     * @return QPS of passed requests
     */
    double passQps();

    /**
     * Get block request per second.
     * 每秒获取块请求。
     * @return QPS of blocked requests
     */
    double blockQps();

    /**
     * Get {@link #passQps()} + {@link #blockQps()} request per second.
     * 每秒获取passQps() + blockQps()请求。
     * @return QPS of passed and blocked requests
     */
    double totalQps();

    /**
     * Get {@link Entry#exit()} request per second.
     * 每秒获取Entry.exit()请求。
     * @return QPS of completed requests
     */
    double successQps();

    /**
     * Get estimated max success QPS till now.
     * 到目前为止估计最大成功 QPS。
     * @return max completed QPS
     */
    double maxSuccessQps();

    /**
     * Get exception count per second.
     * 获取每秒异常计数
     * @return QPS of exception occurs
     */
    double exceptionQps();

    /**
     * Get average rt per second.
     * 获取每秒平均 rt。
     * @return average response time per second
     */
    double avgRt();

    /**
     * Get minimal response time.
     * 获得最短的响应时间
     * @return recorded minimal response time
     */
    double minRt();

    /**
     * Get current active thread count.
     * 获取当前活动线程数。
     * @return current active thread count
     */
    int curThreadNum();

    /**
     * Get last second block QPS.
     * 获取最后一秒区块的 QPS。
     */
    double previousBlockQps();

    /**
     * Last window QPS.
     */
    double previousPassQps();

    /**
     * Fetch all valid metric nodes of resources.
     * 获取资源的所有有效度量节点
     * @return valid metric nodes of resources
     */
    Map<Long, MetricNode> metrics();

    /**
     * Fetch all raw metric items that satisfies the time predicate.
     * 获取满足时间谓词的所有原始指标项
     * @param timePredicate time predicate
     * @return raw metric items that satisfies the time predicate
     * @since 1.7.0
     */
    List<MetricNode> rawMetricsInMin(Predicate<Long> timePredicate);

    /**
     * Add pass count.
     * 添加通过计数。
     * @param count count to add pass
     */
    void addPassRequest(int count);

    /**
     * Add rt and success count.
     * 添加 rt 和成功计数。
     * @param rt      response time
     * @param success success count to add
     */
    void addRtAndSuccess(long rt, int success);

    /**
     * Increase the block count.
     * 增加块数。
     * @param count count to add
     */
    void increaseBlockQps(int count);

    /**
     * Add the biz exception count.
     * 添加业务例外计数。
     * @param count count to add
     */
    void increaseExceptionQps(int count);

    /**
     * Increase current thread count.
     * 增加当前线程数
     */
    void increaseThreadNum();

    /**
     * Decrease current thread count.
     * 减少当前线程数。
     */
    void decreaseThreadNum();

    /**
     * Reset the internal counter. Reset is needed when {@link IntervalProperty#INTERVAL} or
     * {@link SampleCountProperty#SAMPLE_COUNT} is changed.
     * 重置内部计数器。当IntervalProperty.INTERVAL或SampleCountProperty.SAMPLE_COUNT更改时需要重置。
     */
    void reset();
}
