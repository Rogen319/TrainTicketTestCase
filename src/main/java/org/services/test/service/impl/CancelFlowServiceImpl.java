package org.services.test.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.services.test.config.ClusterConfig;
import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.services.test.entity.constants.ServiceConstant;
import org.services.test.entity.dto.*;
import org.services.test.service.CancelFlowService;
import org.services.test.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CancelFlowServiceImpl implements CancelFlowService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ClusterConfig clusterConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto dto, HttpHeaders httpHeaders) {
        HttpEntity<LoginRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/login");
        ResponseEntity<LoginResponseDto> resp = restTemplate.exchange(url, HttpMethod.POST, req,
                LoginResponseDto.class);

        HttpHeaders responseHeaders = resp.getHeaders();
        List<String> values = responseHeaders.get(ServiceConstant.SET_COOKIE);
        List<String> respCookieValue = new ArrayList<>();
        for (String cookie : values) {
            respCookieValue.add(cookie.split(";")[0]);
        }
        LoginResponseDto ret = resp.getBody();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(ServiceConstant.COOKIE, respCookieValue);
        ret.setHeaders(headers);
        return resp;
    }

    @Override
    public ResponseEntity<List<Order>> queryOrder(OrderQueryRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<OrderQueryRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/order/query");
        ResponseEntity<List<Order>> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                new ParameterizedTypeReference<List<Order>>() {
                });
        return ret;
    }

    @Override
    public ResponseEntity<List<Order>> queryOrderOther(OrderQueryRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<OrderQueryRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/orderOther/query");
        ResponseEntity<List<Order>> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                new ParameterizedTypeReference<List<Order>>() {
                });
        return ret;
    }

    @Override
    public ResponseEntity<RefundResponseDto> calculateRefund(RefundRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<RefundRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/cancelCalculateRefund");
        ResponseEntity<RefundResponseDto> ret = restTemplate.exchange(url, HttpMethod.POST, req, RefundResponseDto
                .class);
        return ret;
    }

    @Override
    public ResponseEntity<BasicMessage> cancelOrder(CancelOrderRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<CancelOrderRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/cancelOrder");
        ResponseEntity<BasicMessage> ret = restTemplate.exchange(url, HttpMethod.POST, req, BasicMessage.class);
        return ret;
    }

    @Override
    public FlowTestResult cancelFlow() {
        String testCaseID = UUIDUtil.generateUUID();

        /******************
         * 1st step: login
         *****************/
        String loginTraceId = UUIDUtil.generateUUID();

        LoginRequestDto loginRequestDto = ParamUtil.constructLoginRequestDto();

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.add(ServiceConstant.COOKIE, "YsbCaptcha=C480E98E3B734C438EC07CD4EB72AB21");
        loginHeaders.add(ServiceConstant.TEST_CASE_ID, testCaseID);
        loginHeaders.add(ServiceConstant.TEST_TRACE_ID, loginTraceId);
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<LoginResponseDto> loginResponseDtoResp = login(loginRequestDto, loginHeaders);
        LoginResponseDto loginResponseDto = loginResponseDtoResp.getBody();

        TestTrace testTrace = new TestTrace();
        testTrace.setEntryApi("/login");
        testTrace.setEntryService("ts-login-service");
        testTrace.setEntryTimestamp(System.currentTimeMillis());
        testTrace.setError(AssertUtil.assertByStatusCode(loginResponseDtoResp.getStatusCodeValue()));
        testTrace.setExpected_result(0);
        try {
            testTrace.setReq_param(objectMapper.writeValueAsString(loginRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace.setTestCaseId(testCaseID);
        testTrace.setTestClass("CancelFlowTestClass");
        testTrace.setTestMethod("login");
        testTrace.setTestTraceId(loginTraceId);
        System.out.println(testTrace);

        // set headers
        // login service will set 2 cookies: login and loginToken, this is mandatory for some other service
        Map<String, List<String>> headers = loginResponseDto.getHeaders();
        headers.put(ServiceConstant.TEST_CASE_ID, Arrays.asList(testCaseID));

        // construct test case info
        TestCase testCase = new TestCase();
        testCase.setUserId(loginRequestDto.getEmail());
        testCase.setSessionId(headers.get(ServiceConstant.COOKIE).toString());
        testCase.setTestCaseId(testCaseID);
        testCase.setUserDetail("user details");
        testCase.setUserType("normal");

        /***************************
         * 2nd step: query tickets
         ***************************/

        String queryOrderTraceId = UUIDUtil.generateUUID();
        OrderQueryRequestDto orderQueryRequestDto = new OrderQueryRequestDto();
        orderQueryRequestDto.disableBoughtDateQuery();
        orderQueryRequestDto.disableStateQuery();
        orderQueryRequestDto.disableTravelDateQuery();

        headers.put(ServiceConstant.TEST_TRACE_ID, Arrays.asList(queryOrderTraceId));
        ResponseEntity<List<Order>> queryOrderResponseDtosResp = queryOrder(orderQueryRequestDto, headers);
        List<Order> orders = queryOrderResponseDtosResp.getBody();

        TestTrace testTrace2 = new TestTrace();
        testTrace2.setEntryApi("/order/query");
        testTrace2.setEntryService("ts-order-service");
        testTrace2.setEntryTimestamp(System.currentTimeMillis());
        testTrace2.setError(AssertUtil.assertByStatusCode(queryOrderResponseDtosResp.getStatusCodeValue()));
        testTrace2.setExpected_result(0);
        try {
            testTrace2.setReq_param(objectMapper.writeValueAsString(orderQueryRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace2.setTestCaseId(testCaseID);
        testTrace2.setTestClass("CancelFlowTestClass");
        testTrace2.setTestMethod("queryOrder");
        testTrace2.setTestTraceId(queryOrderTraceId);
        System.out.println(testTrace2);
        /*******************************
         * 3rd step: query order other
         ******************************/

        String queryOrderOtherTraceId = UUIDUtil.generateUUID();

        headers.put(ServiceConstant.TEST_TRACE_ID, Arrays.asList(queryOrderOtherTraceId));
        ResponseEntity<List<Order>> orderOthersResp = queryOrderOther(orderQueryRequestDto, headers);
        List<Order> orderOthers = orderOthersResp.getBody();

        TestTrace testTrace3 = new TestTrace();
        testTrace3.setEntryApi("/orderOther/query");
        testTrace3.setEntryService("ts-order-other-service");
        testTrace3.setEntryTimestamp(System.currentTimeMillis());
        testTrace3.setError(AssertUtil.assertByStatusCode(orderOthersResp.getStatusCodeValue()));
        testTrace3.setExpected_result(0);
        try {
            testTrace3.setReq_param(objectMapper.writeValueAsString(orderOthers));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace3.setTestCaseId(testCaseID);
        testTrace3.setTestClass("BookingFlowTestClass");
        testTrace3.setTestMethod("queryOrderOther");
        testTrace3.setTestTraceId(queryOrderOtherTraceId);
        System.out.println(testTrace3);

        /*************************************
         * 4th step: calculate refund
         *************************************/

        if (null != orderOthers && !orderOthers.isEmpty()) {
            orders.addAll(orderOthers);
        }

        // get order ids that status is pay or not pay.
        List<String> orderIds = orders.stream().filter(order -> order.getStatus() == 0 || order.getStatus() == 1)
                .map(order -> order.getId().toString()).collect(Collectors.toList());
        System.out.println(orderIds.toString());

        String orderId = RandomUtil.getRandomElementInList(orderIds);

        String calculateRefundTraceId = UUIDUtil.generateUUID();

        headers.put(ServiceConstant.TEST_TRACE_ID, Arrays.asList(calculateRefundTraceId));
        RefundRequestDto refundRequestDto = new RefundRequestDto();
        refundRequestDto.setOrderId(orderId);

        ResponseEntity<RefundResponseDto> refundResponseDtoResp = calculateRefund(refundRequestDto, headers);
        RefundResponseDto refundResponseDto = refundResponseDtoResp.getBody();

        TestTrace testTrace4 = new TestTrace();
        testTrace4.setEntryApi("/cancelCalculateRefund");
        testTrace4.setEntryService("ts-cancel-service");
        testTrace4.setEntryTimestamp(System.currentTimeMillis());
        testTrace4.setError(AssertUtil.assertByStatusCode(refundResponseDtoResp.getStatusCodeValue()));
        testTrace4.setExpected_result(0);
        try {
            testTrace4.setReq_param(objectMapper.writeValueAsString(refundRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace4.setTestCaseId(testCaseID);
        testTrace4.setTestClass("CancelFlowTestClass");
        testTrace4.setTestMethod("calculateRefund");
        testTrace4.setTestTraceId(calculateRefundTraceId);
        System.out.println(testTrace4);

        /***************************
         * 5th step: cancel order
         ***************************/
        String cancelTraceId = UUIDUtil.generateUUID();

        headers.put(ServiceConstant.TEST_TRACE_ID, Arrays.asList(cancelTraceId));
        CancelOrderRequestDto cancelOrderRequestDto = new CancelOrderRequestDto();
        cancelOrderRequestDto.setOrderId(orderId);

        ResponseEntity<BasicMessage> basicMessageResp = cancelOrder(cancelOrderRequestDto, headers);
        BasicMessage basicMessage = basicMessageResp.getBody();

        TestTrace testTrace5 = new TestTrace();
        testTrace5.setEntryApi("/cancelOrder");
        testTrace5.setEntryService("ts-cancel-service");
        testTrace5.setEntryTimestamp(System.currentTimeMillis());
        testTrace5.setError(AssertUtil.assertByStatusCode(basicMessageResp.getStatusCodeValue()));
        testTrace5.setExpected_result(0);
        try {
            testTrace5.setReq_param(objectMapper.writeValueAsString(cancelOrderRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace5.setTestCaseId(testCaseID);
        testTrace5.setTestClass("CancelFlowTestClass");
        testTrace5.setTestMethod("cancelOrder");
        testTrace5.setTestTraceId(cancelTraceId);
        System.out.println(testTrace5);

        // construct response
        List<TestTrace> traces = new ArrayList<>();
        traces.add(testTrace);
        traces.add(testTrace2);
        traces.add(testTrace3);
        traces.add(testTrace4);
        traces.add(testTrace5);

        FlowTestResult bftr = new FlowTestResult();
        bftr.setTestCase(testCase);
        bftr.setTestTraces(traces);
        return bftr;
    }
}