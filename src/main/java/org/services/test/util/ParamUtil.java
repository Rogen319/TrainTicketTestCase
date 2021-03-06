package org.services.test.util;

import org.apache.commons.lang.time.DateUtils;
import org.services.test.entity.constants.ServiceConstant;
import org.services.test.entity.dto.*;
import org.services.test.entity.enums.FoodEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ParamUtil {
    /**********************************************
     * generate random parameter for test method
     **********************************************/
    public static LoginRequestDto constructLoginRequestDto() {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("fdse_microservices@163.com");
        loginRequestDto.setPassword("DefaultPassword");
        loginRequestDto.setVerificationCode("abcd");

//        LoginRequestDto loginRequestDto = new LoginRequestDto();
//        loginRequestDto.setEmail("kylinxiang@fudan.edu.com");
//        loginRequestDto.setPassword("123456");
//        loginRequestDto.setVerificationCode("abcd");
        return loginRequestDto;
    }

    public static QueryTicketRequestDto constructQueryTicketReqDto() {
        QueryTicketRequestDto queryTicketRequestDto = new QueryTicketRequestDto();

        // random select end station nanjing or suzhou
        queryTicketRequestDto.setStartingPlace(ServiceConstant.SHANG_HAI);
        if (RandomUtil.getRandomTrueOrFalse()) {
            queryTicketRequestDto.setEndPlace(ServiceConstant.NAN_JING);
        } else {
            queryTicketRequestDto.setEndPlace(ServiceConstant.SU_ZHOU);
        }
        // select tomorrow
        Calendar car = Calendar.getInstance();
        car.add(Calendar.DAY_OF_MONTH, +1);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        queryTicketRequestDto.setDepartureTime(simpleDateFormat.format(car.getTime()));
        return queryTicketRequestDto;
    }

    public static String getRandomContact(List<Contact> contacts) {
        if (contacts.isEmpty()) {
            return null;
        } else {
            return RandomUtil.getRandomElementInList(contacts).getId();
        }

    }

    public static ExcuteRequestDto constructExecuteRequestDto(String orderId) {
        ExcuteRequestDto excuteRequestDto = new ExcuteRequestDto();
        excuteRequestDto.setOrderId(orderId);
        return excuteRequestDto;
    }

    public static CollectRequestDto constructCollectRequestDto(String orderId) {
        CollectRequestDto collectRequestDto = new CollectRequestDto();
        collectRequestDto.setOrderId(orderId);
        return collectRequestDto;
    }

    public static PaymentRequestDto constructPaymentRequestDto(String tripId, String orderId) {
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto();
        paymentRequestDto.setOrderId(orderId);
        paymentRequestDto.setTripId(tripId);
        return paymentRequestDto;
    }

    public static ConfirmRequestDto constructConfirmRequestDto(String departureTime, String startingStation, String
            endingStation, String tripId, String contactId) {
        ConfirmRequestDto confirmRequestDto = new ConfirmRequestDto();
        confirmRequestDto.setContactsId(contactId);
        confirmRequestDto.setTripId(tripId);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        confirmRequestDto.setSeatType(2); // seat type 2, firstClassSeat
        try {
            confirmRequestDto.setDate(simpleDateFormat.parse(departureTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        confirmRequestDto.setFrom(startingStation);
        confirmRequestDto.setTo(endingStation);

        if (RandomUtil.getRandomTrueOrFalse()) {
            confirmRequestDto.setAssurance(0); // 不选保险
        } else {
            confirmRequestDto.setAssurance(1); // 选保险
        }

        if (RandomUtil.getRandomTrueOrFalse()) {
            confirmRequestDto.setFoodType(0); // 不选吃的
        } else {
            confirmRequestDto.setFoodType(1); // 选type 1
            if (RandomUtil.getRandomTrueOrFalse()) {
                confirmRequestDto.setFoodName(FoodEnum.CURD.getName());
                confirmRequestDto.setFoodPrice(FoodEnum.CURD.getPrice());
            } else if (RandomUtil.getRandomTrueOrFalse()) {
                confirmRequestDto.setFoodName(FoodEnum.SOUP.getName());
                confirmRequestDto.setFoodPrice(FoodEnum.SOUP.getPrice());
            } else {
                confirmRequestDto.setFoodName(FoodEnum.NOODLES.getName());
                confirmRequestDto.setFoodPrice(FoodEnum.NOODLES.getPrice());
            }
        }

        // 随机托运
        if (RandomUtil.getRandomTrueOrFalse()) {
            confirmRequestDto.setConsigneeName(RandomUtil.getStringRandom(8));
            confirmRequestDto.setConsigneePhone(RandomUtil.getTel());
            confirmRequestDto.setConsigneeWeight(RandomUtil.getRamdomWeight());
        }

        return confirmRequestDto;
    }

    public static FoodRequestDto constructFoodRequestDto(String departureTime, String startingStation, String
            endingStation, String tripId) {
        FoodRequestDto foodRequestDto = new FoodRequestDto();
        foodRequestDto.setDate(departureTime);
        foodRequestDto.setStartStation(startingStation);
        foodRequestDto.setEndStation(endingStation);
        foodRequestDto.setTripId(tripId);
        return foodRequestDto;
    }

    public static OrderQueryRequestDto constructOrderQueryRequestDto() {
        OrderQueryRequestDto orderQueryRequestDto = new OrderQueryRequestDto();
        orderQueryRequestDto.disableBoughtDateQuery();
        orderQueryRequestDto.disableStateQuery();
        orderQueryRequestDto.disableTravelDateQuery();
        return orderQueryRequestDto;
    }

    public static StationNameRequestDto constructStationNameRequestDto(String stationId) {
        StationNameRequestDto stationNameRequestDto = new StationNameRequestDto();
        stationNameRequestDto.setStationId(stationId);
        return stationNameRequestDto;
    }

    public static ConsignInsertRequestDto constructConsignRequestDto(Order order) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ConsignInsertRequestDto consignInsertRequestDto = new ConsignInsertRequestDto();
        consignInsertRequestDto.setAccountId(order.getAccountId());
        consignInsertRequestDto.setConsignee(RandomUtil.getStringRandom(10));
        consignInsertRequestDto.setFrom(order.getFrom());
        consignInsertRequestDto.setHandleDate(dateFormat.format(order.getTravelDate()));
        consignInsertRequestDto.setWithin(false);
        consignInsertRequestDto.setPhone(RandomUtil.getTel());
        consignInsertRequestDto.setTargetDate(DateUtils.addDays(order.getTravelDate(), 1).toString());
        consignInsertRequestDto.setTo(order.getTo());
        consignInsertRequestDto.setWeight(RandomUtil.getRamdomWeight());
        return consignInsertRequestDto;
    }

    public static VoucherUIRequestDto constructVoucherUIRequestDto(Order order) {
        VoucherUIRequestDto voucherUIRequestDto = new VoucherUIRequestDto();
        voucherUIRequestDto.setOrderId(order.getId().toString());
        voucherUIRequestDto.setTrain_number(order.getTrainNumber());
        return voucherUIRequestDto;
    }

    public static VoucherInfoRequestDto constructVoucherInfoRequestDto(Order order) {
        VoucherInfoRequestDto voucherInfoRequestDto = new VoucherInfoRequestDto();
        voucherInfoRequestDto.setOrderId(order.getId().toString());
        voucherInfoRequestDto.setType(1);
        return voucherInfoRequestDto;
    }

    public static AddContactsInfo constructAddContactsInfo() {
        AddContactsInfo addContactsInfo = new AddContactsInfo();
        addContactsInfo.setDocumentNumber(RandomUtil.getStringRandom(10));
        addContactsInfo.setDocumentType(1);
        addContactsInfo.setName(RandomUtil.getStringRandom(8));
        addContactsInfo.setPhoneNumber(RandomUtil.getTel());
        return addContactsInfo;
    }
}
