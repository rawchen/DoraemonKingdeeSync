package com.lundong.sync;

import com.lundong.sync.entity.bitable.PaymentRequestOne;
import com.lundong.sync.entity.kingdee.AccountingDimension;
import com.lundong.sync.entity.kingdee.Voucher;
import com.lundong.sync.entity.kingdee.VoucherDetail;
import com.lundong.sync.service.SystemService;
import com.lundong.sync.util.SignUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ApplicationTests {

    @Autowired
    private SystemService systemService;

    @Test
    void contextLoads() {
    }

    @Test
    void t01() {
        List<PaymentRequestOne> baseList = SignUtil.findBaseList("N7T2bfpf0a34kWser2mc3rgmnBc", "tblXFhGLneZiJhph", PaymentRequestOne.class);
        for (PaymentRequestOne paymentRequest : baseList) {
            System.out.println(paymentRequest);
        }
    }

    @Test
    void t02() {
        Voucher voucher = new Voucher();
        voucher.setBusDate("2023-11-30");
        voucher.setVoucherGroupId("PRE003");
        List<VoucherDetail> list = new ArrayList<>();
        VoucherDetail j = new VoucherDetail();
        VoucherDetail d = new VoucherDetail();

        j.setExplanation("11月30日测试");
        j.setAccountId("1151.07.01");
        j.setDebit("1000");
        j.setCredit("0");

        d.setExplanation("11月30日测试");
        d.setAccountId("1002.01.02");
        d.setDebit("0");
        d.setCredit("1000");

        list.add(j);
        list.add(d);
        voucher.setVoucherDetails(list);
        String s = SignUtil.saveVoucher(voucher);
        System.out.println("编号：" + s);
    }

    @Test
    void t03() {
        List<HttpCookie> httpCookies = SignUtil.loginCookies();
        for (HttpCookie httpCookie : httpCookies) {
            System.out.println(httpCookie);
        }
    }

    @Test
    void t04() {
        String result = SignUtil.getFeishuUserName("fa222fd1");
        System.out.println(result);
    }

    @Test
    void t05() {
        AccountingDimension accountingDimension = new AccountingDimension();
        accountingDimension.setFflex4("123");
        try {
            Class<?> clazz = accountingDimension.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value;
                value = field.get(accountingDimension);
                if (value == null) {
                    field.set(accountingDimension, "");
                }
            }
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(accountingDimension);
    }
}
