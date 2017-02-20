package com.jm.bid.admin.tender.service;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.tender.mapper.TenderMapper;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.persistence.Page;
import com.jm.bid.boot.util.BeanMapper;
import com.jm.bid.boot.util.JsonMapper;
import com.jm.bid.boot.validator.BeanValidators;
import com.jm.bid.common.dto.CoalBaseDTO;
import com.jm.bid.common.dto.TenderDTO;
import com.jm.bid.common.dto.TenderParamDTO;
import com.jm.bid.common.entity.Tender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiangyang on 2016/12/5.
 */
@Service
@Transactional(readOnly = false)
public class TenderService {

    private static final Logger logger = LoggerFactory.getLogger(TenderService.class);

    /*煤种字典*/
    public static final Map<Integer, String> coalTypeDic = new LinkedHashMap<>();

    /*煤矿字典*/
    public static final Map<Integer, String> coalZoneDic = new LinkedHashMap<>();

    /*发站字典*/
    public static final Map<Integer, String> forwardStationDic = new LinkedHashMap<>();

    /*运输方式字典*/
    public static final Map<Integer, String> transportModeDic = new LinkedHashMap<>();

    /*运输方式字典*/
    public static final Map<Integer, String> tenderStatusDic = new LinkedHashMap<>();

    @Autowired
    private TenderMapper tenderMapper;


    static {
        coalTypeDic.put(1, "末煤");
        coalTypeDic.put(2, "块煤");
        coalTypeDic.put(3, "洗末煤");

        coalZoneDic.put(1, "成庄矿");
        coalZoneDic.put(2, "寺河矿");
        coalZoneDic.put(3, "长平矿");
        coalZoneDic.put(4, "赵庄矿");

        forwardStationDic.put(1, "晋城北");
        forwardStationDic.put(2, "西阳村");
        forwardStationDic.put(3, "嘉峰");
        forwardStationDic.put(4, "东田良");

        transportModeDic.put(1, "汽车");
        transportModeDic.put(2, "火车");


        tenderStatusDic.put(1, "草稿");
        tenderStatusDic.put(2, "预告");
        tenderStatusDic.put(3, "竞价中");
        tenderStatusDic.put(4, "未中标 - 结束");
        tenderStatusDic.put(5, "有中标 - 结束");

    }


    @Transactional(readOnly = false)
    public void addTempTender(TenderDTO tenderDTO, AdminDTO adminDTO) {
        tenderDTO.setStatus(1);
        this.addTender(tenderDTO, adminDTO);
    }

    @Transactional(readOnly = false)
    public void addRealTender(TenderDTO tenderDTO, AdminDTO adminDTO) {
        tenderDTO.setStatus(tenderDTO.getTenderStartDate().isAfter(LocalDateTime.now()) ? 2 : 3);
        this.addTender(tenderDTO, adminDTO);
    }

    private void addTender(TenderDTO tenderDto, AdminDTO adminDTO) {
        BeanValidators.validateWithException(tenderDto);
        if (tenderMapper.isExistsTenderTitle(tenderDto.getTenderTitle())) {
            throw new BusinessException("竞价标题已经存在!");
        }
        if (tenderDto.getTenderStartDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("竞价开始时间应该大于当前时间!");
        }
        if (tenderDto.getTenderEndDate().isBefore(tenderDto.getTenderStartDate())) {
            throw new BusinessException("竞价截止时间不能在竞价开始时间之前!");
        }
        Tender tender = BeanMapper.map(tenderDto, Tender.class);
        tender.setCreateBy(adminDTO.getSecurePhone());
        tender.setProjectStr(JsonMapper.nonEmptyMapper().toJson(tenderDto.getCoalBaseDTO()));
        tender.setReleaseDate(LocalDateTime.now());
        tenderMapper.insertSelective(tender);
    }

    @Transactional(readOnly = false)
    public void updateTenderForTempSave(TenderDTO tenderDTO, AdminDTO adminDTO) {
        tenderDTO.setStatus(1);
        this.updateTender(tenderDTO, adminDTO);
    }

    @Transactional(readOnly = false)
    public void updateTenderForRealRelease(TenderDTO tenderDTO, AdminDTO adminDTO) {
        tenderDTO.setStatus(tenderDTO.getTenderStartDate().isAfter(LocalDateTime.now()) ? 2 : 3);
        this.updateTender(tenderDTO, adminDTO);
    }


    private void updateTender(TenderDTO tenderDto, AdminDTO adminDTO) {
        BeanValidators.validateWithException(tenderDto);

        if (tenderMapper.isExistsTenderTitleExclude(tenderDto.getTenderTitle(), tenderDto.getId())) {
            throw new BusinessException("采购标题已经存在!");
        }

        if (tenderDto.getTenderStartDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("竞价开始时间应该大于当前时间!");
        }
        if (tenderDto.getTenderEndDate().isBefore(tenderDto.getTenderStartDate())) {
            throw new BusinessException("竞价截止时间不能在竞价开始时间之前!");
        }

        Tender tender = BeanMapper.map(tenderDto, Tender.class);
        tender.setProjectStr(JsonMapper.nonEmptyMapper().toJson(tenderDto.getCoalBaseDTO()));
        tender.setCreateBy(adminDTO.getSecurePhone());
        tender.setReleaseDate(LocalDateTime.now());
        tenderMapper.updateByPrimaryKeySelective(tender);
    }

    @Transactional(readOnly = false)
    public void updateTender(Tender tender) {
        tenderMapper.updateByPrimaryKeySelective(tender);
    }

    @Transactional(readOnly = false)
    public void deleteTender(long id, AdminDTO adminDTO) {

        Tender tender = this.loadTenderById(id);
        if (tender == null || tender.getStatus() == 0) {
            throw new NotFoundException();
        }
        if (this.isProcessingTender(tender.getStatus())) {
            throw new BusinessException("竞价项目正在进行中,不能删除!");
        }
        tender.setStatus(0);
        tenderMapper.updateByPrimaryKeySelective(tender);
    }

    @Transactional(readOnly = false)
    public void backOutTender(long id, AdminDTO adminDTO) {
        Tender tender = this.loadTenderById(id);
        if (tender == null || tender.getStatus() == 0) {
            throw new NotFoundException();
        }
        if (tender.getStatus() != 2) {
            if (tender.getStatus() > 2) {
                throw new BusinessException("竞价项目正在进行中,不能撤销!");
            } else if (tender.getStatus() == 1) {
                throw new BusinessException("竞价项目处于草稿中!");
            }
        }
        tender.setStatus(1);
        tenderMapper.updateByPrimaryKeySelective(tender);
    }

    public Tender loadTenderById(long tenderId) {
        Tender tender = tenderMapper.selectByPrimaryKey(tenderId);
        if (tender == null || tender.getStatus() == 0) {
            return null;
        }
        return tender;
    }

    public TenderDTO loadTenderDTOById(long tenderId) {
        Tender tender = tenderMapper.selectByPrimaryKey(tenderId);
        if (tender == null || tender.getStatus() == 0) {
            return null;
        }
        TenderDTO tenderDTO = BeanMapper.map(tender, TenderDTO.class);
        tenderDTO.setCoalBaseDTO(JsonMapper.nonEmptyMapper().fromJson(tender.getProjectStr(), CoalBaseDTO.class));
        return tenderDTO;
    }


    public boolean isProcessingTender(int status) {
        if (status >= 2 && status <= 5) {
            return true;
        }
        return false;

    }

    /**
     * @return 竞标状态: 0 删除  1 草稿 2 预告 3 竞价中 4 有中标-结束 5 未中标-结束
     * key: status 标识  value:数据条数
     */
    public Map<Integer, Long> loadStatusCount() {
        List<Tender> tenderParamDTOs = tenderMapper.loadAllTender();
        Map<Integer, Long> statusMap = new HashMap<>();
        statusMap.put(1, tenderParamDTOs.parallelStream().filter(t -> t.getStatus() == 1).count());
        statusMap.put(2, tenderParamDTOs.parallelStream().filter(t -> t.getStatus() == 2).count());
        statusMap.put(3, tenderParamDTOs.parallelStream().filter(t -> t.getStatus() == 3).count());
        statusMap.put(4, tenderParamDTOs.parallelStream().filter(t -> t.getStatus() == 4 || t.getStatus() == 5).count());
        return statusMap;
    }

    /**
     * 通过状态查询招标数据
     *
     * @param status 参数如果不传,就查询所有
     * @return
     */
    public List<Tender> loadAllTenderByStatus(Integer... status) {
        return tenderMapper.loadAllTender(status);
    }

    /**
     * 查询所有招标带分页
     *
     * @param tenderParamDTO
     * @return
     */
    public Page<Tender> findAllTender(TenderParamDTO tenderParamDTO) {
        return tenderMapper.findAllTender(tenderParamDTO);
    }

    /**
     * 查询所有进行中的招标带分页
     *
     * @param tenderParamDTO
     * @return
     */
    public Page<Tender> findAllTenderInProcessing(TenderParamDTO tenderParamDTO) {
        return tenderMapper.findAllTenderInProcessing(tenderParamDTO);
    }

    public List<Tender> loadWaitReleaseNotice() {
        return tenderMapper.loadWaitReleaseNotice();
    }

    public void updateVisitorsCount(long tenderId) {
        tenderMapper.updateVisitorsCount(tenderId);
    }

}
