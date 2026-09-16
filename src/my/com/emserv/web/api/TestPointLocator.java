package my.com.emserv.web.api;

import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.PointLocatorVO;

public class TestPointLocator {
    public void test(DataPointVO dp) {
        PointLocatorVO locator = dp.getPointLocator();
        boolean s = locator.isSettable();
        System.out.println("isSettable: " + s);
    }
}
