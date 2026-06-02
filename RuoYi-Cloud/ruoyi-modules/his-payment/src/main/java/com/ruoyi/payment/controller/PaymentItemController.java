package com.ruoyi.payment.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.payment.domain.PaymentItem;
import com.ruoyi.payment.service.IPaymentItemService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 收费明细Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/item")
public class PaymentItemController extends BaseController
{
    @Autowired
    private IPaymentItemService paymentItemService;

    /**
     * 查询收费明细列表
     */
    @RequiresPermissions("payment:item:list")
    @GetMapping("/list")
    public TableDataInfo list(PaymentItem paymentItem)
    {
        startPage();
        List<PaymentItem> list = paymentItemService.selectPaymentItemList(paymentItem);
        return getDataTable(list);
    }

    /**
     * 导出收费明细列表
     */
    @RequiresPermissions("payment:item:export")
    @Log(title = "收费明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PaymentItem paymentItem)
    {
        List<PaymentItem> list = paymentItemService.selectPaymentItemList(paymentItem);
        ExcelUtil<PaymentItem> util = new ExcelUtil<PaymentItem>(PaymentItem.class);
        util.exportExcel(response, list, "收费明细数据");
    }

    /**
     * 获取收费明细详细信息
     */
    @RequiresPermissions("payment:item:query")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable("itemId") Long itemId)
    {
        return success(paymentItemService.selectPaymentItemByItemId(itemId));
    }

    /**
     * 新增收费明细
     */
    @RequiresPermissions("payment:item:add")
    @Log(title = "收费明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PaymentItem paymentItem)
    {
        return toAjax(paymentItemService.insertPaymentItem(paymentItem));
    }

    /**
     * 修改收费明细
     */
    @RequiresPermissions("payment:item:edit")
    @Log(title = "收费明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PaymentItem paymentItem)
    {
        return toAjax(paymentItemService.updatePaymentItem(paymentItem));
    }

    /**
     * 删除收费明细
     */
    @RequiresPermissions("payment:item:remove")
    @Log(title = "收费明细", businessType = BusinessType.DELETE)
	@DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        return toAjax(paymentItemService.deletePaymentItemByItemIds(itemIds));
    }
}
