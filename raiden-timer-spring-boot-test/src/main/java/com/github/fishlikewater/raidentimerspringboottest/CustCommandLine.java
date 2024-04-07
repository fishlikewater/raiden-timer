/*
 * Copyright © 2024 zhangxiang (fishlikewater@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fishlikewater.raidentimerspringboottest;

import com.github.fishlikewater.timer.core.BaseTimerTask;
import com.github.fishlikewater.timer.core.timer.TimerLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * {@code CustCommandLine}
 *
 * @author zhangxiang
 * @date 2024/04/07
 */

@Component
public class CustCommandLine implements CommandLineRunner {

    private final TimerLauncher timerLauncher;

    public CustCommandLine(TimerLauncher timerLauncher) {
        this.timerLauncher = timerLauncher;
    }

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 1; i++) {
            BaseTimerTask task = new BaseTimerTask() {
                private int i = 0;

                @Override
                public void run() {
                    System.out.println(i++);
                    System.out.println(Thread.currentThread());
                    System.out.println(STR."线程ID : \{Thread.currentThread().threadId()}|线程名 : \{Thread.currentThread().getName()}|是否为虚拟线程 : \{Thread.currentThread().isVirtual()}");
                }
            };
            task.setDesc(STR."测试corn任务\{i}");
            task.setCornExpression("0 0/1 * * * ?");

            timerLauncher.add(task);
        }
    }
}
