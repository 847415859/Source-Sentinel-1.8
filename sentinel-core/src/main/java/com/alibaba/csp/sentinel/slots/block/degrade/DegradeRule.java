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
package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.util.Objects;

/**
 * <p>
 * Degrade is used when the resources are in an unstable state, these resources
 * will be degraded within the next defined time window. There are two ways to
 * measure whether a resource is stable or not:
 * </p>
 * <ul>
 * <li>
 * Average response time ({@code DEGRADE_GRADE_RT}): When
 * the average RT exceeds the threshold ('count' in 'DegradeRule', in milliseconds), the
 * resource enters a quasi-degraded state. If the RT of next coming 5
 * requests still exceed this threshold, this resource will be downgraded, which
 * means that in the next time window (defined in 'timeWindow', in seconds) all the
 * access to this resource will be blocked.
 * </li>
 * <li>
 * Exception ratio: When the ratio of exception count per second and the
 * success qps exceeds the threshold, access to the resource will be blocked in
 * the coming window.
 * </li>
 * </ul>
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class DegradeRule extends AbstractRule {

    public DegradeRule() {}

    public DegradeRule(String resourceName) {
        setResource(resourceName);
    }

    /**
     * Circuit breaking strategy (0: average RT, 1: exception ratio, 2: exception count).
     * 熔断策略  0-慢调用比例 1-异常比例 2-异常数
     */
    private int grade = RuleConstant.DEGRADE_GRADE_RT;

    /**
     * Threshold count. The exact meaning depends on the field of grade.
     * <ul>
     *     <li>In average RT mode, it means the maximum response time(RT) in milliseconds.</li>
     *     <li>In exception ratio mode, it means exception ratio which between 0.0 and 1.0.</li>
     *     <li>In exception count mode, it means exception count</li>
     * <ul/>
     *
     * 阈值计数。确切的含义取决于等级领域。
     * - 在平均 RT 模式下，它表示最大响应时间 (RT)，以毫秒为单位。
     * - 在例外率模式下，表示例外率，范围在0.0 到1.0 之间。
     * - 异常计数模式下，表示异常计数
     */
    private double count;

    /**
     * Recovery timeout (in seconds) when circuit breaker opens. After the timeout, the circuit breaker will
     * transform to half-open state for trying a few requests.
     * 断路器打开时的恢复超时（以秒为单位）。超时后，断路器将转换为半开状态以尝试一些请求。
     */
    private int timeWindow;

    /**
     * Minimum number of requests (in an active statistic time span) that can trigger circuit breaking.
     * 能够触发熔断的最小请求数（活跃统计时间段内）
     * @since 1.7.0
     */
    private int minRequestAmount = RuleConstant.DEGRADE_DEFAULT_MIN_REQUEST_AMOUNT;

    /**
     * The threshold of slow request ratio in RT mode.
     * RT模式下慢请求比例阈值
     * @since 1.8.0
     */
    private double slowRatioThreshold = 1.0d;

    /**
     * The interval statistics duration in millisecond.
     * 间隔统计持续时间（以毫秒为单位）。
     * @since 1.8.0
     */
    private int statIntervalMs = 1000;

    public int getGrade() {
        return grade;
    }

    public DegradeRule setGrade(int grade) {
        this.grade = grade;
        return this;
    }

    public double getCount() {
        return count;
    }

    public DegradeRule setCount(double count) {
        this.count = count;
        return this;
    }

    public int getTimeWindow() {
        return timeWindow;
    }

    public DegradeRule setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
        return this;
    }

    public int getMinRequestAmount() {
        return minRequestAmount;
    }

    public DegradeRule setMinRequestAmount(int minRequestAmount) {
        this.minRequestAmount = minRequestAmount;
        return this;
    }

    public double getSlowRatioThreshold() {
        return slowRatioThreshold;
    }

    public DegradeRule setSlowRatioThreshold(double slowRatioThreshold) {
        this.slowRatioThreshold = slowRatioThreshold;
        return this;
    }

    public int getStatIntervalMs() {
        return statIntervalMs;
    }

    public DegradeRule setStatIntervalMs(int statIntervalMs) {
        this.statIntervalMs = statIntervalMs;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }
        DegradeRule rule = (DegradeRule)o;
        return Double.compare(rule.count, count) == 0 &&
            timeWindow == rule.timeWindow &&
            grade == rule.grade &&
            minRequestAmount == rule.minRequestAmount &&
            Double.compare(rule.slowRatioThreshold, slowRatioThreshold) == 0 &&
            statIntervalMs == rule.statIntervalMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), count, timeWindow, grade, minRequestAmount,
            slowRatioThreshold, statIntervalMs);
    }

    @Override
    public String toString() {
        return "DegradeRule{" +
            "resource=" + getResource() +
            ", grade=" + grade +
            ", count=" + count +
            ", limitApp=" + getLimitApp() +
            ", timeWindow=" + timeWindow +
            ", minRequestAmount=" + minRequestAmount +
            ", slowRatioThreshold=" + slowRatioThreshold +
            ", statIntervalMs=" + statIntervalMs +
            '}';
    }
}
