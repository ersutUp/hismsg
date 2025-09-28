package xyz.ersut.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.ersut.message.dto.Result;
import xyz.ersut.message.entity.SysDictType;
import xyz.ersut.message.service.SysDictTypeService;

import jakarta.validation.Valid;

/**
 * 字典类型控制器
 * 
 * @author ersut
 */
@Slf4j
@RestController
@RequestMapping("/api/dict/type")
@RequiredArgsConstructor
@Tag(name = "字典类型管理", description = "字典类型的增删改查操作")
public class SysDictTypeController {
    
    private final SysDictTypeService dictTypeService;
    
    /**
     * 分页查询字典类型
     * 
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param dictName 字典名称
     * @param dictType 字典类型
     * @return 分页结果
     */
    @Operation(summary = "分页查询字典类型", description = "根据条件分页查询字典类型列表")
    @GetMapping("/list")
    public Result<Page<SysDictType>> list(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页大小", example = "10") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "字典名称", example = "消息类型") @RequestParam(required = false) String dictName,
            @Parameter(description = "字典类型", example = "message_type") @RequestParam(required = false) String dictType) {
        try {
            Page<SysDictType> page = dictTypeService.selectPage(pageNum, pageSize, dictName, dictType);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询字典类型失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }
    
    /**
     * 根据ID查询字典类型详情
     * 
     * @param id 字典类型ID
     * @return 字典类型信息
     */
    @Operation(summary = "查询字典类型详情", description = "根据ID查询字典类型的详细信息")
    @GetMapping("/{id}")
    public Result<SysDictType> getById(
            @Parameter(description = "字典类型ID", example = "1") @PathVariable Long id) {
        try {
            SysDictType dictType = dictTypeService.getById(id);
            if (dictType == null) {
                return Result.error("字典类型不存在");
            }
            return Result.success(dictType);
        } catch (Exception e) {
            log.error("查询字典类型详情失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }
    
    /**
     * 新增字典类型
     * 
     * @param dictType 字典类型信息
     * @return 操作结果
     */
    @Operation(summary = "新增字典类型", description = "创建新的字典类型")
    @PostMapping
    public Result<Void> save(
            @Parameter(description = "字典类型信息") @Valid @RequestBody SysDictType dictType) {
        try {
            boolean success = dictTypeService.save(dictType);
            if (success) {
                return Result.success("新增成功", null);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            log.error("新增字典类型失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 修改字典类型
     * 
     * @param dictType 字典类型信息
     * @return 操作结果
     */
    @Operation(summary = "修改字典类型", description = "更新现有的字典类型信息")
    @PutMapping
    public Result<Void> update(
            @Parameter(description = "字典类型信息") @Valid @RequestBody SysDictType dictType) {
        try {
            boolean success = dictTypeService.update(dictType);
            if (success) {
                return Result.success("修改成功", null);
            } else {
                return Result.error("修改失败");
            }
        } catch (Exception e) {
            log.error("修改字典类型失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除字典类型
     * 
     * @param id 字典类型ID
     * @return 操作结果
     */
    @Operation(summary = "删除字典类型", description = "根据ID删除指定的字典类型")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "字典类型ID", example = "1") @PathVariable Long id) {
        try {
            boolean success = dictTypeService.deleteById(id);
            if (success) {
                return Result.success("删除成功", null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除字典类型失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}