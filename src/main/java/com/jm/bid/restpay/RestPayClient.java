package com.jm.bid.restpay;

import com.google.common.collect.Maps;
import com.jm.bid.boot.persistence.Page;
import com.jm.bid.boot.util.JsonMapper;
import com.jm.bid.restpay.dto.CashAccountDTO;
import com.jm.bid.restpay.dto.PrintFlowDTO;
import com.jm.bid.restpay.dto.TransferDTO;
import com.jm.bid.restpay.dto.TransferStatusDTO;
import com.jm.bid.user.finance.dto.PrintFlowParamDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiangyang on 2016/11/30.
 */
@Service
public class RestPayClient {

    private static final Logger logger = LoggerFactory.getLogger(RestPayClient.class);

    @Value("${pg.restAddress}")
    private String restAddress;

    @Autowired
    RestTemplate restTemplate;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 创建系统内账户
     *
     * @param companyId 公司id
     * @return {
     * "accId": "string",
     * "status": 1,
     * "errMess": "string"
     * }
     */
    public CashAccountDTO createAccount(String companyId) {
        final String url = restAddress + "/pg/createAccount";
        Map<String, Object> requestBody = Maps.newHashMap();
        // idType = 2 默认为用户id
        requestBody.put("idType", 2);
        // idValue  默认用户id
        requestBody.put("idValue", companyId);
        requestBody.put("operator", "jm_system_create");
        System.out.println(JsonMapper.nonEmptyMapper().toJson(requestBody));
        return restTemplate.postForEntity(url, requestBody, CashAccountDTO.class).getBody();
    }

    /**
     * 创建中信资金账户
     *
     * @param accountName 账户名称
     * @param accId       系统资金账户id
     * @return {
     * "accNo": "string",
     * "status": 1,
     * "errMess": "string"
     * }
     */
    public CashAccountDTO createZXBankAccount(String accountName, String accId) {
        final String url = restAddress + "/pg/createBankAccount";
        Map<String, Object> requestBody = Maps.newHashMap();
        requestBody.put("name", accountName);
        requestBody.put("bankId", 1);
        requestBody.put("accId", accId);
        requestBody.put("operator", "jm_system_create");
        return restTemplate.postForEntity(url, requestBody, CashAccountDTO.class).getBody();
    }


    /**
     * 查询是否开通账户成功
     *
     * @param accId 系统账户id
     * @param accNo 银行资金账号
     * @return {
     * "status": 1,
     * "errMess": "string"
     * }
     */
    public TransferStatusDTO queryAccountStatus(String accId, String accNo) {
        final String url = restAddress + "/pg/queryAccount";
        Map<String, Object> requestBody = Maps.newHashMap();
        requestBody.put("accId", accId);
        requestBody.put("accNo", accNo);
        requestBody.put("bankId", 1);
        requestBody.put("operator", "jm_system_create");
        return restTemplate.postForEntity(url, requestBody, TransferStatusDTO.class).getBody();
    }

    /**
     * 查询账户余额
     *
     * @param accId 系统账户id
     * @param accNo 银行资金账号
     * @return {
     * "errMess": "string",
     * "status": 1,
     * "sjAmount": 0,
     * "kyAmount": 0,
     * "djAmount": 0
     * }
     */
    public CashAccountDTO queryCashAccountBalance(String accId, String accNo) {
        final String url = restAddress + "/pg/queryBalance";
        Map<String, Object> requestBody = Maps.newHashMap();
        requestBody.put("accId", accId);
        requestBody.put("accNo", accNo);
        requestBody.put("operator", "jm_system_create");
        return restTemplate.postForEntity(url, requestBody, CashAccountDTO.class).getBody();
    }

    /**
     * 交易状态查询
     *
     * @param clientNo 交易流水号
     * @return {
     * "status": 1,
     * "message": "string"
     * }
     */
    public TransferStatusDTO queryTransferStatus(String clientNo) {
        final String url = restAddress + "/pg/transferQuery";
        Map<String, Object> requestBody = Maps.newHashMap();
        requestBody.put("clientNo", clientNo);
        return restTemplate.postForEntity(url, requestBody, TransferStatusDTO.class).getBody();
    }

    /**
     * 资金账户转账
     * {
     * "targetAccNm": "string",
     * "srcAccId": "string",
     * "srcAccNm": "string",
     * "srcAccNo": "string",
     * "clientId": "string",
     * "targetAccId": "string",
     * "targetAccNo": "string",
     * "amount": 0,
     * "operator": "string",
     * "bankId": 1,
     * "memo": "string"
     * }
     *
     * @param transferDTO
     * @return {
     * "status": 1,
     * "message": "string"
     * }
     */
    public TransferStatusDTO cashAccountTransfer(TransferDTO transferDTO) {
        final String url = restAddress + "/pg/transferSame";
        return restTemplate.postForEntity(url, transferDTO, TransferStatusDTO.class).getBody();
    }


    /**
     * 提现
     * <p>
     * {
     * "sameBank": 0,
     * "srcAccId": "string",
     * "srcAccNo": "string",
     * "srcAccNm": "string",
     * "targetAccNo": "string",
     * "targetAccNm": "string",
     * "clientId": "string",
     * "amount": 0,
     * "targetBankName": "string",
     * "targetSubBranchName": "string",
     * "targetZFLHH": "string",
     * "bankId": 1,
     * "operator": "string",
     * "memo": "string"
     * }
     *
     * @param transferDTO
     * @return {
     * "status": 1,
     * "message": "string"
     * }
     */
    public TransferStatusDTO withdraw(TransferDTO transferDTO) {
        final String url = restAddress + "/pg/transferDiff";
        return restTemplate.postForEntity(url, transferDTO, TransferStatusDTO.class).getBody();
    }


    public Page<PrintFlowDTO> findPrintFlow(String accId, String accNo, PrintFlowParamDTO paramDTO) {
        final String url = restAddress + "/pg/printFlowQuery";

        Map<String, Object> requestParam = new HashMap<String, Object>();
        int offset = (paramDTO.getPageNo() - 1) * paramDTO.getPageSize();
        int limit = paramDTO.getPageSize();

        requestParam.put("accNo", accNo);
        requestParam.put("accId", accId);
        requestParam.put("limit", limit);
        requestParam.put("offset", offset);
        requestParam.put("bankId", 1);

        if (paramDTO.getStartDate() != null) {
            requestParam.put("start", formatter.format(paramDTO.getStartDate()));
        }
        if (paramDTO.getEndDate() != null) {
            requestParam.put("end", formatter.format(paramDTO.getEndDate()));
        }

        if (paramDTO.getTradeType() != null) {
            requestParam.put("flag", paramDTO.getTradeType());
        }

        PrintFlowResult printFlowResult = restTemplate.postForEntity(url, requestParam, PrintFlowResult.class).getBody();
        Page<PrintFlowDTO> page = Page.createInstance();
        page.setResults(printFlowResult.getPrintFlowList());
        page.setTotalRecord(printFlowResult.getCount());
        page.setPageNo(paramDTO.getPageNo());
        page.setPageSize(paramDTO.getPageSize());
        return page;

    }

    static class PrintFlowResult {
        private List<PrintFlowDTO> printFlowList;
        private int count;

        public List<PrintFlowDTO> getPrintFlowList() {
            return printFlowList;
        }

        public void setPrintFlowList(List<PrintFlowDTO> printFlowList) {
            this.printFlowList = printFlowList;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }


}
