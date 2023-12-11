package com.lundong.sync.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.ApprovalInstance;
import com.lundong.sync.entity.feishu.FeishuUser;
import com.lundong.sync.entity.kingdee.KingdeeParam;
import com.lundong.sync.entity.kingdee.Voucher;
import com.lundong.sync.entity.kingdee.VoucherDetail;
import com.lundong.sync.enums.CurrencyIdEnum;
import com.lundong.sync.enums.ExchangeRateTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.HttpCookie;
import java.time.LocalDate;
import java.util.*;

/**
 * @author RawChen
 * @date 2023-06-25 14:33
 */
@Slf4j
public class SignUtil {

    /**
     * 飞书自建应用获取tenant_access_token
     */
    public static String getAccessToken(String appId, String appSecret) {
        JSONObject object = new JSONObject();
        object.put("app_id", appId);
        object.put("app_secret", appSecret);
        String resultStr = HttpRequest.post("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal")
                .form(object)
                .execute().body();
        if (StringUtils.isNotEmpty(resultStr)) {
            JSONObject resultObject = (JSONObject) JSON.parse(resultStr);
            if (!"0".equals(resultObject.getString("code"))) {
                return "";
            } else {
                String tenantAccessToken = resultObject.getString("tenant_access_token");
                if (tenantAccessToken != null) {
                    return tenantAccessToken;
                }
            }
        }
        return "";
    }

    /**
     * 获取飞书用户姓名
     *
     * @param accessToken
     * @return
     */
    public static String getFeishuUserName(String accessToken, String userId) {

        Map<String, Object> param = new HashMap<>();
        param.put("user_id_type", "user_id");
        param.put("department_id_type", "department_id");
        FeishuUser feishuUser = new FeishuUser();
        try {
            String resultStr = HttpRequest.get("https://open.feishu.cn/open-apis/contact/v3/users/" + userId)
                    .header("Authorization", "Bearer " + accessToken)
                    .form(param)
                    .execute()
                    .body();
            log.info("获取飞书用户姓名接口: {}", resultStr);
            JSONObject jsonObject = JSONObject.parseObject(resultStr);
            if (jsonObject.getInteger("code") == 0) {
                JSONObject user = jsonObject.getJSONObject("data").getJSONObject("user");
                if (user != null) {
                    feishuUser.setUserId(user.getString("user_id"));
                    feishuUser.setName(user.getString("name"));
                }
                return feishuUser.getName();
            } else {
                log.error("获取飞书用户姓名接口失败: {}", resultStr);
                return "";
            }
        } catch (Exception e) {
            log.error("获取飞书用户姓名接口异常: ", e);
            return "";
        }
    }

    /**
     * 获取飞书用户
     *
     * @return
     */
    public static String getFeishuUserName(String userId) {
        String accessToken = getAccessToken(Constants.APP_ID_FEISHU, Constants.APP_SECRET_FEISHU);
        return getFeishuUserName(accessToken, userId);
    }

    /**
     * 获取单个审批实例详情
     *
     * @param accessToken
     * @param instanceId
     * @return
     */
    public static ApprovalInstance approvalInstanceDetail(String accessToken, String instanceId) {
        try {
            JSONObject object = new JSONObject();
            object.put("user_id_type", "user_id");
            String resultStr = HttpRequest.get("https://open.feishu.cn/open-apis/approval/v4/instances/" + instanceId)
                    .header("Authorization", "Bearer " + accessToken)
                    .form(object)
                    .execute().body();
            log.info("获取单个审批实例详情接口: {}", resultStr);
            if (StringUtils.isNotEmpty(resultStr)) {
                JSONObject resultObject = JSON.parseObject(resultStr);
                if (resultObject.getInteger("code") == 0) {
                    return resultObject.getJSONObject("data").toJavaObject(ApprovalInstance.class);
                } else {
                    log.error("获取单个审批实例详情接口出错: {}", resultStr);
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("获取单个审批实例详情异常: ", e);
            return null;
        }
        return null;
    }

    /**
     * 获取单个审批实例详情
     *
     * @param instanceId
     * @return
     */
    public static ApprovalInstance approvalInstanceDetail(String instanceId) {
        String accessToken = getAccessToken(Constants.APP_ID_FEISHU, Constants.APP_SECRET_FEISHU);
        return approvalInstanceDetail(accessToken, instanceId);
    }

    /**
     * 列出记录
     *
     * @param accessToken
     * @param appToken
     * @param tableId
     * @return
     */
    public static <T> List<T> findBaseList(String accessToken, String appToken, String tableId, Class<T> tClass) {
        List<T> results = new ArrayList<>();
        Map<String, Object> param = new HashMap<>();
        boolean hasMore = true;
        try {
            while (hasMore) {
                param.put("page_size", 500);
                String resultStr = HttpRequest.get("https://open.feishu.cn/open-apis/bitable/v1/apps/" + appToken + "/tables/" + tableId + "/records")
                        .header("Authorization", "Bearer " + accessToken)
                        .form(param)
                        .execute()
                        .body();
                log.info("列出记录接口: {}", resultStr.length() > 100 ? resultStr.substring(0, 100) + "..." : resultStr);
                Thread.sleep(500L);
                JSONObject jsonObject = JSON.parseObject(resultStr);
                if (jsonObject.getInteger("code") != 0) {
                    log.error("列出记录接口调用失败");
                    return Collections.emptyList();
                }
                JSONObject data = (JSONObject) jsonObject.get("data");
                JSONArray items = (JSONArray) data.get("items");
                for (int i = 0; i < items.size(); i++) {
                    JSONObject records = items.getJSONObject(i).getJSONObject("fields");
                    T testEntity;
                    testEntity = JSONObject.toJavaObject(records, tClass);
                    StringUtil.bracketReplace(testEntity);
                    results.add(testEntity);
                }
                if ((boolean) data.get("has_more")) {
                    param.put("page_token", data.getString("page_token"));
                } else {
                    hasMore = false;
                }
            }
        } catch (Exception e) {
            log.info("列出记录接口调用异常", e);
            return Collections.emptyList();
        }
        return results;
    }

    /**
     * 获取多维表列表
     *
     * @param appToken
     * @param tableId
     * @return
     */
    public static <T> List<T> findBaseList(String appToken, String tableId, Class<T> tClass) {
        String accessToken = getAccessToken(Constants.APP_ID_FEISHU, Constants.APP_SECRET_FEISHU);
        return findBaseList(accessToken, appToken, tableId, tClass);
    }

    public static List<HttpCookie> loginCookies() {
        KingdeeParam param = new KingdeeParam();
        param.setAcctId(Constants.ACCT_ID);
        param.setKingdeeUrl(Constants.KINGDEE_API);
        param.setUsername(Constants.USERNAME);
        param.setPassword(Constants.PASSWORD);
        String loginUrl = StringUtil.convertUrl(param.getKingdeeUrl()) + Constants.KINGDEE_LOGIN;
        String loginJson = "{\n" +
                "    \"acctID\": \"" + param.getAcctId() + "\",\n" +
                "    \"username\": \"" + param.getUsername() + "\",\n" +
                "    \"password\": \"" + param.getPassword() + "\",\n" +
                "    \"lcid\": \"2052\"\n" +
                "}";
        HttpResponse loginResponse = HttpRequest.post(loginUrl)
                .body(loginJson)
                .execute();
        log.info("登录金蝶接口: {}", loginResponse.body());
        return loginResponse.getCookies();
    }

    /**
     * 保存记账凭证
     *
     * @return
     */
    public static String saveVoucher(Voucher voucher) {
        String saveVoucherData = "{\"formid\":\"GL_VOUCHER\",\"data\":{\"Model\":{\"FVOUCHERID\":0," +
                "\"FAccountBookID\":{\"FNumber\":\"账簿\"},\"FDate\":\"账期日期\",\"FBUSDATE\":" +
                "\"业务日期\",\"FYEAR\":会计年度,\"FPERIOD\":期间,\"FVOUCHERGROUPID\":{\"FNumber\":\"凭证字\"}," +
                "\"FVOUCHERGROUPNO\":\"凭证号\",\"FATTACHMENTS\":0,\"FISADJUSTVOUCHER\":\"false\"," +
                "\"FSourceBillKey\":{\"FNumber\":\"\"},\"FIMPORTVERSION\":\"\"," +
                "\"FDocumentStatus\":\"审核状态\",\"FEntity\":[分录列表]}}}";

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
//        int year = 2023;
//        int month = 1;

        for (VoucherDetail voucherDetail : voucher.getVoucherDetails()) {
            if (voucherDetail.getAccountId() == null) {
                log.error("凭证列表中存在科目编码为null");
            }
        }

        saveVoucherData = saveVoucherData.replace("账簿", Constants.ACCOUNT_BOOK);
        saveVoucherData = saveVoucherData.replace("账期日期", voucher.getDate());
//        saveVoucherData = saveVoucherData.replace("账期日期", year + "-" + month + "-");
//        saveVoucherData = saveVoucherData.replace("业务日期", voucher.getBusDate());
        saveVoucherData = saveVoucherData.replace("业务日期", "");
        saveVoucherData = saveVoucherData.replace("会计年度", String.valueOf(year));
        saveVoucherData = saveVoucherData.replace("期间", String.valueOf(month));
        saveVoucherData = saveVoucherData.replace("凭证字", voucher.getVoucherGroupId());
        saveVoucherData = saveVoucherData.replace("审核状态", Constants.VOUCHER_DOCUMENT_STATUS);
        saveVoucherData = saveVoucherData.replace("凭证号", "");

        StringBuilder getVoucherDetailsStr = new StringBuilder();
        if (ArrayUtil.isEmpty(voucher.getVoucherDetails())) {
            log.error("入账凭证保存失败，入账凭证明细为空");
            return null;
        }
        for (VoucherDetail voucherDetail : voucher.getVoucherDetails()) {
            if (voucherDetail.getDebit() == null) {
                voucherDetail.setDebit("0");
            }
            if (voucherDetail.getCredit() == null) {
                voucherDetail.setCredit("0");
            }
            if (voucherDetail.getAmountFor() == null) {
                voucherDetail.setAmountFor("0");
            }

            StringUtil.replaceNullFieldToEmpty(voucher.getVoucherDetails());

            String detail = "{\"FEntryID\":0,\"FEXPLANATION\":\"摘要文本\",\"FACCOUNTID\":{\"FNumber\":\"科目编码文本\"}," +
                    "\"FDetailID\":{\"FDETAILID__FFLEX10\":{\"FNumber\":\"\"},\"FDETAILID__FFLEX4\":{\"FNumber\":\"\"},\"FDETAILID__FF100002\":{\"FNumber\":\"\"}}," +
                    "\"FCURRENCYID\":{\"FNumber\":\"币别文本\"},\"FEXCHANGERATETYPE\":{\"FNumber\":\"汇率类型文本\"}," +
                    "\"FEXCHANGERATE\":1,\"FUnitId\":{\"FNUMBER\":\"\"},\"FPrice\":0,\"FQty\":0," +
                    "\"FAMOUNTFOR\":原币金额文本,\"FDEBIT\":借方金额文本,\"FCREDIT\":贷方金额文本,\"FSettleTypeID\":{\"FNumber\":\"\"}," +
                    "\"FSETTLENO\":\"\",\"FBUSNO\":\"\",\"FEXPORTENTRYID\":0,\"FDetailID\": {" +
                    "\"FDETAILID__FFLEX10\": {\"FNumber\": \"核算维度10\"},\"FDETAILID__FFLEX4\": {\"FNumber\": \"核算维度4\"}," +
                    "\"FDETAILID__FFLEX5\": {\"FNumber\": \"核算维度5\"},\"FDETAILID__FFLEX9\": {\"FNumber\": \"核算维度9\"}," +
                    "\"FDETAILID__FFLEX6\": {\"FNumber\": \"核算维度6\"},\"FDETAILID__FFLEX7\": {\"FNumber\": \"核算维度7\"}," +
                    "\"FDETAILID__FFLEX11\": {\"FNumber\": \"核算维度11\"},\"FDETAILID__FFLEX8\": {\"FNumber\": \"核算维度8\"}," +
                    "\"FDETAILID__FFLEX12\": {\"FNumber\": \"核算维度12\"},\"FDETAILID__FFLEX13\": {\"FNumber\": \"核算维度13\"}," +
                    "\"FDETAILID__FF100002\": {\"FNumber\": \"核算维度自定义2\"},\"FDETAILID__FF100003\": {\"FNumber\": \"核算维度自定义3\"}," +
                    "\"FDETAILID__FF100004\": {\"FNumber\": \"核算维度自定义4\"},\"FDETAILID__FF100005\": {\"FNumber\": \"核算维度自定义5\"}}}";
            detail = detail.replace("摘要文本", voucherDetail.getExplanation());
            detail = detail.replace("科目编码文本", voucherDetail.getAccountId());
            detail = detail.replace("币别文本", CurrencyIdEnum.PRE001.getType());
            detail = detail.replace("汇率类型文本", ExchangeRateTypeEnum.HLTX01_SYS.getType());
            detail = detail.replace("借方金额文本", voucherDetail.getDebit());
            detail = detail.replace("贷方金额文本", voucherDetail.getCredit());
            detail = detail.replace("原币金额文本", voucherDetail.getAmountFor());
            detail = detail.replace("核算维度10", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFflex10()));
            detail = detail.replace("核算维度4", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFflex4()));
            detail = detail.replace("核算维度5", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFflex5()));
            detail = detail.replace("核算维度9", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFflex9()));
            detail = detail.replace("核算维度6", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFflex6()));
            detail = detail.replace("核算维度7", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFflex7()));
            detail = detail.replace("核算维度11", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFflex11()));
            detail = detail.replace("核算维度8", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFflex8()));
            detail = detail.replace("核算维度12", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFflex12()));
            detail = detail.replace("核算维度13", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFflex13()));
            detail = detail.replace("核算维度自定义2", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFf100002()));
            detail = detail.replace("核算维度自定义3", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFf100003()));
            detail = detail.replace("核算维度自定义4", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFf100004()));
            detail = detail.replace("核算维度自定义5", StringUtil.nullIsEmpty(voucherDetail.getAccountingDimension().getFf100005()));
            getVoucherDetailsStr.append(detail);
            getVoucherDetailsStr.append(",");
        }

        if (getVoucherDetailsStr.toString().endsWith(",")) {
            getVoucherDetailsStr = new StringBuilder(getVoucherDetailsStr.substring(0, getVoucherDetailsStr.toString().lastIndexOf(',')));
        }

        saveVoucherData = saveVoucherData.replace("分录列表", getVoucherDetailsStr);
        log.info("保存入账凭证参数: {}", saveVoucherData);
        try {
            List<HttpCookie> httpCookies = loginCookies();
            String resultStr = HttpRequest.post(Constants.KINGDEE_API + Constants.KINGDEE_SAVE)
                    .body(saveVoucherData)
                    .cookie(httpCookies)
                    .execute().body();
            log.info("金蝶凭证保存接口: {}", resultStr);
            JSONObject postObject = JSONObject.parseObject(resultStr);
            JSONObject resultObject = (JSONObject) postObject.get("Result");
            JSONObject responseStatus = (JSONObject) resultObject.get("ResponseStatus");
            if (responseStatus.getBoolean("IsSuccess")) {
                return resultObject.getString("Number");
            } else {
                log.error("金蝶凭证保存接口错误: {}", resultStr);
                return null;
            }
        } catch (Exception e) {
            log.error("金蝶凭证保存异常: ", e);
        }
        return null;
    }

}
