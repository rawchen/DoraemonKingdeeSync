package com.lundong.sync.controller;

import com.lark.oapi.core.utils.Jsons;
import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.ApprovalInstanceFormResult;
import com.lundong.sync.enums.ApprovalInstanceEnum;
import com.lundong.sync.enums.DataTypeEnum;
import com.lundong.sync.event.ApprovalInstanceStatusUpdatedEvent;
import com.lundong.sync.event.ApprovalInstanceStatusUpdatedV1Handler;
import com.lundong.sync.event.CustomEventDispatcher;
import com.lundong.sync.event.CustomServletAdapter;
import com.lundong.sync.service.SystemService;
import com.lundong.sync.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author RawChen
 * @date 2023-03-02 11:19
 */
@Slf4j
@RestController
@RequestMapping
public class EventController {

    @Autowired
    private CustomServletAdapter servletAdapter;

    @Autowired
    private SystemService systemService;

    // 注册消息处理器
    private final CustomEventDispatcher EVENT_DISPATCHER = CustomEventDispatcher
            .newBuilder(Constants.VERIFICATION_TOKEN, Constants.ENCRYPT_KEY)
            .onApprovalInstanceStatusUpdatedV1(new ApprovalInstanceStatusUpdatedV1Handler() {
                @Override
                public void handle(ApprovalInstanceStatusUpdatedEvent event) throws Exception {
                    log.info("ApprovalInstanceStatusUpdatedV1: {}", StringUtil.subLog(Jsons.DEFAULT.toJson(event)));
                    Runnable worker = () -> {
                        // 获取审批实例code
                        String approvalCode = event.getEvent().getApprovalCode();
                        // 判断通过的审批code是否为定义的五个单，不是就退出
                        if (!Constants.PAYMENT_PERSONAL_PREPAID_APPROVAL_CODE.equals(approvalCode)
                                && !Constants.INVOICING_APPLICATION_APPROVAL_CODE.equals(approvalCode)
                                && !Constants.INVOICE_WRITE_OFF_APPROVAL_CODE.equals(approvalCode)
                                && !Constants.WITHHOLDING_APPLICATION_APPROVAL_CODE.equals(approvalCode)
                                && !Constants.REFUND_APPLICATION_APPROVAL_CODE.equals(approvalCode)) {
                            return;
                        }
                        String instanceCode = event.getEvent().getInstanceCode();
                        String status = event.getEvent().getStatus();
                        String instanceOperateTime = event.getEvent().getInstanceOperateTime();

                        if (ApprovalInstanceEnum.APPROVED.getType().equals(status)) {
                            // 根据审批实例ID查询审批单
                            ApprovalInstanceFormResult result = StringUtil.instanceToFormList(instanceCode);
                            String save = systemService.processApprovalForm(result, DataTypeEnum.toType(approvalCode).getDesc(), instanceCode, instanceOperateTime);
                            if (save != null && !"empty".equals(save)) {
                                systemService.insertRecordLog(result, save, instanceOperateTime);
                            }
                        }
                    };
                    Constants.queue.submitTask(worker);
                }
            })
            .build();

    /**
     * 飞书订阅事件回调
     *
     * @param request
     * @param response
     * @throws Throwable
     */
    @RequestMapping(value = "/feishu/webhook/event")
    public void event(HttpServletRequest request, HttpServletResponse response)
            throws Throwable {
        servletAdapter.handleEvent(request, response, EVENT_DISPATCHER);
    }
}