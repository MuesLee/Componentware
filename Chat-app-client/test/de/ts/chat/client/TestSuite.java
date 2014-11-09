package de.ts.chat.client;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.ts.chat.server.beans.CommonStatisticManagementBeanTest;

@RunWith(Suite.class)
@SuiteClasses({ ServiceHandlerImplTest.class,
		CommonStatisticManagementBeanTest.class })
public class TestSuite {

}
