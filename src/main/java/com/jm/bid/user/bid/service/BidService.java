package com.jm.bid.user.bid.service;

import com.jm.bid.admin.finance.service.RefundRecordService;
import com.jm.bid.admin.tender.service.TenderService;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.persistence.Page;
import com.jm.bid.boot.util.BeanMapper;
import com.jm.bid.common.entity.Tender;
import com.jm.bid.common.service.SMS;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.bid.dto.BidDTO;
import com.jm.bid.user.bid.entity.Bid;
import com.jm.bid.user.bid.entity.BidSubscribe;
import com.jm.bid.user.bid.mapper.BidMapper;
import com.jm.bid.user.bid.mapper.BidSubscribeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xiangyang on 2016/12/20.
 */
@Service
@Transactional(readOnly = false)
public class BidService {

    @Autowired
    private BidMapper bidMapper;

    @Autowired
    private TenderService tenderService;

    @Autowired
    private RefundRecordService refundRecordService;

    @Autowired
    private SMS sms;

    @Autowired
    private BidSubscribeMapper bidSubscribeMapper;

    /**
     * 提交报价
     *
     * @param tenderId
     * @param quotePrice
     * @param quoteAmount
     * @param userDTO
     */
    @Transactional(readOnly = false)
    public void insertBid(long tenderId, BigDecimal quotePrice, Integer quoteAmount, UserDTO userDTO) {
        Bid bid = new Bid();
        bid.setCompanyId(userDTO.getCompanyId());
        bid.setCompanyName(userDTO.getCompanyName());
        bid.setCreateBy(userDTO.getSecurePhone());
        bid.setTenderId(tenderId);
        bid.setQuotePrice(quotePrice);
        bid.setQuoteAmount(quoteAmount);
        bidMapper.insertSelective(bid);
    }

    /**
     * 加载所有投标记录
     *
     * @param bidDTO
     * @param companyId
     * @return
     */
    public Page<BidDTO> findByCompanyId(BidDTO bidDTO, String companyId) {
        bidDTO.setCompanyId(companyId);
        return bidMapper.findCompanyBid(bidDTO);
    }

    public List<Bid> selectNoBidCompany(long tenderId) {
        return bidMapper.selectNoBidCompany(tenderId);
    }

    /**
     * 加载当前公司最新的提交记录
     *
     * @param tenderId
     * @param companyId
     * @return
     */
    public BidDTO selectByCompanyIdAndTenderId(long tenderId, String companyId) {
        return bidMapper.selectByCompanyIdAndTenderId(tenderId, companyId);
    }

    /**
     * 通过招标id加载投标
     *
     * @param tenderId
     * @return
     */
    public List<BidDTO> selectBidByTenderId(long tenderId) {
        return bidMapper.selectByTenderId(tenderId);
    }

    /**
     * 查询当前报价最高价
     *
     * @param tenderId
     * @return
     */
    @Transactional(readOnly = false)
    public BigDecimal selectMaxPriceByTenderId(long tenderId) {
        BigDecimal maxQuotePrice = bidMapper.selectMaxPriceByTenderId(tenderId);
        if (maxQuotePrice == null) {
            Tender tender = tenderService.loadTenderById(tenderId);
            if (tender == null) {
                throw new NotFoundException();
            } else {
                return tender.getSaleBasePrice();
            }
        }
        return maxQuotePrice;
    }

    /**
     * 价格优先   时间优先
     * 1、 剩余竞买量 <= 最低投标量    不中标
     * 2、 剩余竞买量 >= 最低投标量    则中标,按照此规则依次匹配后续报价,如果符合中标
     *
     * @param tenderId
     */
    @Transactional(readOnly = false)
    public void autoAnswerBid(long tenderId) {

        Tender tender = tenderService.loadTenderById(tenderId);

        List<Bid> bids = BeanMapper.mapList(this.selectBidByTenderId(tenderId).stream()
                .collect(Collectors.toList()), Bid.class);

        if (bids.size() > 0) {

            // 发售量
            int temp = tender.getSaleAmount();
            //提报量<=发布量的情况
            if (bids.stream().mapToInt(Bid::getQuoteAmount).sum() <= tender.getSaleAmount()) {
                bids.parallelStream().forEach(b -> {
                    // 中标量为提报量
                    b.setWinBidAmount(b.getQuoteAmount());
                    b.setChecked(true);
                    bidMapper.updateByPrimaryKeySelective(b);
                });

            } else {
                for (int i = 0; i < bids.size(); i++) {
                    Bid bid = bids.get(i);
                    if ( temp - bid.getQuoteAmount() >= 0) {
                        // 中标量为提报量
                        bid.setChecked(true);
                        bid.setWinBidAmount(bid.getQuoteAmount());
                        bidMapper.updateByPrimaryKeySelective(bid);
                        temp -= bid.getQuoteAmount();
                    } else {

                        //1分钟之内报价的总量
                        final int totalQualityInOneMinute = bids.stream().filter(a ->
                                a.getQuotePrice().compareTo(bid.getQuotePrice()) == 0 &&
                                        a.getCreateDate().truncatedTo(ChronoUnit.MINUTES).compareTo(bid.getCreateDate().truncatedTo(ChronoUnit.MINUTES)) == 0)
                                .mapToInt(Bid::getQuoteAmount).sum();


                        //剩余量 = 剩余量 + 所有1分钟之内的中标量
                        final int residueQuality =  bids.stream().filter(a -> a.getQuotePrice().compareTo(bid.getQuotePrice()) == 0 &&
                                a.getCreateDate().truncatedTo(ChronoUnit.MINUTES).compareTo(bid.getCreateDate().truncatedTo(ChronoUnit.MINUTES)) == 0)
                                .filter(a->a.getChecked()!=null && a.getChecked()==true)
                                .mapToInt(Bid::getWinBidAmount).sum() + temp;

                        //计算中标量比重
                        bids.stream().filter(a ->
                                bid.getQuotePrice().compareTo(a.getQuotePrice()) == 0 &&
                                        a.getCreateDate().truncatedTo(ChronoUnit.MINUTES).compareTo(bid.getCreateDate().truncatedTo(ChronoUnit.MINUTES)) == 0)
                                .forEach(b -> {

                                    // 提报量 / 1分钟之内的提报总量 = 占比  (四舍五入保留2位小数)
                                    double proportion = Double.valueOf(String.valueOf(
                                            BigDecimal.valueOf(b.getQuoteAmount()).divide(BigDecimal.valueOf(totalQualityInOneMinute), 2, RoundingMode.HALF_UP)) );

                                    // 剩余量 * 比率 = 实际量
                                    int winBidQuality = Double.valueOf(residueQuality * proportion).intValue() / 100 * 100;

                                    if(winBidQuality>=100){
                                        b.setWinBidAmount(winBidQuality);
                                        b.setChecked(true);
                                        bidMapper.updateByPrimaryKeySelective(b);
                                    }
                                });

                        //修改未中标
                        bids.stream().filter(a -> a.getChecked() == null)
                                .forEach(a -> {
                                    a.setChecked(false);
                                    bidMapper.updateByPrimaryKeySelective(a);
                                });

                        break;

                    }
                }


            }
            //短信通知客户
            refundRecordService.refundDeposit(tenderId);
            tender.setStatus(5);
            tenderService.updateTender(tender);
        } else {
            tender.setStatus(4);
            tenderService.updateTender(tender);
        }
        //短信通知
        this.tenderResultNoticeSms(tenderId, tender.getTenderCode());
    }


    public BigDecimal selectTotalSaleMoney() {
        return bidMapper.selectTotalSaleMoney();
    }

    public Long selectTotalSaleAmount() {
        return bidMapper.selectTotalSaleAmount();
    }

    final String subscribeStr = "您好,您已经预约编号为{0}的竞价,请登陆平台及时查看!";

    //通知预约短信
    public void noticeSubscribe(long tenderId) {
        Tender tender = tenderService.loadTenderById(tenderId);
        bidSubscribeMapper.selectByTenderId(tenderId).parallelStream()
                .filter(b -> b.getNoticeDate() == null)
                .forEach(b -> {
                    sms.sendSMS(b.getPhone(), MessageFormat.format(subscribeStr, tender.getTenderCode()));
                    b.setNoticeDate(LocalDateTime.now());
                    bidSubscribeMapper.updateByPrimaryKey(b);
                });
    }

    public int bidSubscribeCount(long tenderId) {
        return bidSubscribeMapper.selectByTenderId(tenderId).size();
    }

    //添加预约记录
    @Transactional(readOnly = false)
    public void addBidSubscribe(long tenderId, String phone, String ip, boolean logined) {
        if (bidSubscribeMapper.selectByTenderIdAndPhone(tenderId, phone) != null) {
            throw new BusinessException("您已经预约成功,请不要重复预约!");
        }
        BidSubscribe bidSubscribe = new BidSubscribe();
        bidSubscribe.setTenderId(tenderId);
        bidSubscribe.setPhone(phone);
        bidSubscribe.setIp(ip);
        bidSubscribe.setLogined(logined);
        bidSubscribe.setCreateDate(LocalDateTime.now());
        bidSubscribeMapper.insert(bidSubscribe);
    }

    public static final String winBid_sms = "尊敬的用户您好:根据晋煤竞价规则的要求，贵公司对于竞价公告编号为：{0}的报价已中标，请登录平台查看详情，并给予晋煤工作人员联系洽谈签订购销合同事宜。谢谢!";

    public static final String failBid_sms = "尊敬的用户您好:根据晋煤竞价规则的要求，贵公司对于竞价公告编号为：{0}的报价未中标，感谢您的积极参与！也期待您的下一次合作！请登录平台查看详情。谢谢!";

    //扣款成功通知短信
    private void tenderResultNoticeSms(long tenderId, String tenderCode) {
        this.selectBidByTenderId(tenderId).parallelStream()
                .filter(b -> b.getChecked() != null)
                .forEach(b -> {

                    if (b.getChecked()) {
                        sms.sendSMS(b.getCreateBy(), MessageFormat.format(winBid_sms, tenderCode));
                    } else {
                        sms.sendSMS(b.getCreateBy(), MessageFormat.format(failBid_sms, tenderCode));
                    }
                });
    }


}
