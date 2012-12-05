/*
 * Copyright 2012 Brian Thomas Matthews
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

package com.btmatthews.maven.plugins.ldap.mojo;

import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Timer;
import java.util.TimerTask;

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.Monitor;
import org.apache.maven.plugin.Mojo;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

/**
 * Unit tests for the Mojo that implements the run goal.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public class TestRunMojo {

    /**
     * Mock the logger.
     */
    @Mock
    private Logger logger;

    /**
     * The mojo being tested.
     */
    private Mojo mojo;

    /**
     * Used to create temporary directories used by the unit tests.
     */
    @Rule
    public TemporaryFolder outputDirectory = new TemporaryFolder();

    /**
     * Prepare for test execution by initialising the mock objects and test fixture.
     *
     * @throws Exception If there was an error configuring the test fixture.
     */
    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mojo = new RunMojo();
        ReflectionUtils.setVariableValueInObject(mojo, "monitorPort", 11389);
        ReflectionUtils.setVariableValueInObject(mojo, "rootDn", "dc=btmatthews,dc=com");
        ReflectionUtils.setVariableValueInObject(mojo, "ldapPort", 10389);
        ReflectionUtils.setVariableValueInObject(mojo, "monitorKey", "ldap");
        ReflectionUtils.setVariableValueInObject(mojo, "outputDirectory", outputDirectory.getRoot());
    }

    /**
     * Verify that we can start the server.
     *
     * @throws Exception If there was an error.
     */
    @Test
    public void testRun() throws Exception {
        ReflectionUtils.setVariableValueInObject(mojo, "daemon", Boolean.FALSE);

        final Thread mojoThread = new Thread(new Runnable() {
            public void run() {
                try {
                    mojo.execute();
                } catch (final Exception e) {
                }
            }
        });
        mojoThread.start();

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                signalStop();
            }
        }, 5000L);

        mojoThread.join(15000L);
    }

    /**
     * Verify that we can start the server as a daemon.
     *
     * @throws Exception If there was an error.
     */
    @Test
    public void testRunDaemon() throws Exception {
        ReflectionUtils.setVariableValueInObject(mojo, "daemon", Boolean.TRUE);
        mojo.execute();
        Thread.sleep(5000L);
        signalStop();
    }

    /**
     * Send a stop signal to monitor controlling the server.
     */
    private void signalStop() {
        new Monitor("ldap", 11389).sendCommand("stop", logger);
    }
}