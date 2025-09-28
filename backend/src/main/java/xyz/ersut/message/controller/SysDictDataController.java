package xyz.ersut.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.ersut.message.dto.Result;
import xyz.ersut.message.entity.SysDictData;
import xyz.ersut.message.service.SysDictDataService;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 字典数据控制器
 * 
 * @author ersut
 */
@Slf4j
@RestController
@RequestMapping("/api/dict/data")
@RequiredArgsConstructor
@Tag(name = "字典数据管理", description = "字典数据的增删改查操作")
public class SysDictDataController {
    
    private final SysDictDataService dictDataService;
    
    /**
     * 分页查询字典数据
     * 
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param dictType 字典类型
     * @param dictLabel 字典标签
     * @return 分页结果
     */
    @Operation(summary = "分页查询字典数据", description = "根据条件分页查询字典数据列表")
    @GetMapping("/list")
    public Result<Page<SysDictData>> list(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页大小", example = "10") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "字典类型", example = "message_type") @RequestParam(required = false) String dictType,
            @Parameter(description = "字典标签", example = "通知消息") @RequestParam(required = false) String dictLabel) {
        try {
            Page<SysDictData> page = dictDataService.selectPage(pageNum, pageSize, dictType, dictLabel);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询字典数据失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }
    
    /**
     * 根据字典类型查询字典数据
     * 
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    @Operation(summary = "根据类型查询字典数据", description = "根据字典类型查询所有相关的字典数据")
    @GetMapping("/type/{dictType}")
    public Result<List<SysDictData>> getByType(
            @Parameter(description = "字典类型", example = "message_type") @PathVariable String dictType) {
        try {
            List<SysDictData> list = dictDataService.getByDictType(dictType);
            return Result.success(list);
        } catch (Exception e) {
            log.error("根据类型查询字典数据失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }
    
    /**
     * 根据ID查询字典数据详情
     * 
     * @param id 字典数据ID
     * @return 字典数据信息
     */
    @Operation(summary = "查询字典数据详情", description = "根据ID查询字典数据的详细信息")
    @GetMapping("/{id}")
    public Result<SysDictData> getById(
            @Parameter(description = "字典数据ID", example = "1") @PathVariable Long id) {
        try {
            SysDictData dictData = dictDataService.getById(id);
            if (dictData == null) {
                return Result.error("字典数据不存在");
            }
            return Result.success(dictData);
        } catch (Exception e) {
            log.error("查询字典数据详情失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }
    
    /**
     * 新增字典数据
     * 
     * @param dictData 字典数据信息
     * @return 操作结果
     */
    @Operation(summary = "新增字典数据", description = "创建新的字典数据项")
    @PostMapping
    public Result<Void> save(
            @Parameter(description = "字典数据信息") @Valid @RequestBody SysDictData dictData) {
        try {
            boolean success = dictDataService.save(dictData);
            if (success) {
                return Result.success("新增成功", null);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            log.error("新增字典数据失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 修改字典数据
     * 
     * @param dictData 字典数据信息
     * @return 操作结果
     */
    @Operation(summary = "修改字典数据", description = "更新现有的字典数据信息")
    @PutMapping
    public Result<Void> update(
            @Parameter(description = "字典数据信息") @Valid @RequestBody SysDictData dictData) {
        try {
            boolean success = dictDataService.update(dictData);
            if (success) {
                return Result.success("修改成功", null);
            } else {
                return Result.error("修改失败");
            }
        } catch (Exception e) {
            log.error("修改字典数据失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除字典数据
     * 
     * @param id 字典数据ID
     * @return 操作结果
     */
    @Operation(summary = "删除字典数据", description = "根据ID删除指定的字典数据")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "字典数据ID", example = "1") @PathVariable Long id) {
        try {
            boolean success = dictDataService.deleteById(id);
            if (success) {
                return Result.success("删除成功", null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除字典数据失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据字典类型和值获取标签
     * 
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典标签
     */
    @Operation(summary = "获取字典标签", description = "根据字典类型和值获取对应的字典标签")
    @GetMapping("/label")
    public Result<String> getDictLabel(
            @Parameter(description = "字典类型", example = "message_type") @RequestParam String dictType,
            @Parameter(description = "字典值", example = "notification") @RequestParam String dictValue) {
        try {
            String label = dictDataService.getDictLabel(dictType, dictValue);
            return Result.success(label);
        } catch (Exception e) {
            log.error("获取字典标签失败: {}", e.getMessage());
            return Result.error("获取失败");
        }
    }
}