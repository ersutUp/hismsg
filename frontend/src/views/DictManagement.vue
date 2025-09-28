<template>
  <div class="page-container">
    <div class="page-header">
      <h1>字典管理</h1>
      <p class="page-description">管理系统字典类型和字典数据</p>
    </div>
    
    <el-row :gutter="20">
      <!-- 字典类型 -->
      <el-col :xs="24" :lg="10">
        <el-card class="dict-type-card">
          <template #header>
            <div class="card-header">
              <span>字典类型</span>
              <el-button
                type="primary"
                size="small"
                :icon="Plus"
                @click="showAddTypeDialog"
              >
                新增类型
              </el-button>
            </div>
          </template>
          
          <!-- 类型搜索 -->
          <el-form inline class="mb-3">
            <el-form-item>
              <el-input
                v-model="typeSearchForm.dictName"
                placeholder="字典名称"
                clearable
                @clear="searchDictTypes"
                @keyup.enter="searchDictTypes"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :icon="Search"
                @click="searchDictTypes"
              >
                查询
              </el-button>
            </el-form-item>
          </el-form>
          
          <!-- 类型列表 -->
          <el-table
            v-loading="typeLoading"
            :data="dictTypeList"
            size="small"
            highlight-current-row
            @current-change="handleTypeSelect"
          >
            <el-table-column label="字典名称" prop="dictName" show-overflow-tooltip />
            <el-table-column label="字典类型" prop="dictType" show-overflow-tooltip />
            <el-table-column label="状态" width="60">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button
                  text
                  type="primary"
                  size="small"
                  @click="editDictType(row)"
                >
                  编辑
                </el-button>
                <el-button
                  text
                  type="danger"
                  size="small"
                  @click="deleteDictType(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 类型分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="typePagination.pageNum"
              v-model:page-size="typePagination.pageSize"
              :total="typePagination.total"
              :page-sizes="[10, 20, 50]"
              layout="prev, pager, next, sizes"
              small
              @size-change="fetchDictTypes"
              @current-change="fetchDictTypes"
            />
          </div>
        </el-card>
      </el-col>
      
      <!-- 字典数据 -->
      <el-col :xs="24" :lg="14">
        <el-card class="dict-data-card">
          <template #header>
            <div class="card-header">
              <span>
                字典数据
                <span v-if="currentDictType" class="current-type">
                  （{{ currentDictType.dictName }}）
                </span>
              </span>
              <el-button
                type="primary"
                size="small"
                :icon="Plus"
                :disabled="!currentDictType"
                @click="showAddDataDialog"
              >
                新增数据
              </el-button>
            </div>
          </template>
          
          <div v-if="!currentDictType" class="empty-state">
            <el-empty description="请先选择字典类型" />
          </div>
          
          <div v-else>
            <!-- 数据搜索 -->
            <el-form inline class="mb-3">
              <el-form-item>
                <el-input
                  v-model="dataSearchForm.dictLabel"
                  placeholder="字典标签"
                  clearable
                  @clear="searchDictData"
                  @keyup.enter="searchDictData"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  :icon="Search"
                  @click="searchDictData"
                >
                  查询
                </el-button>
              </el-form-item>
            </el-form>
            
            <!-- 数据列表 -->
            <el-table
              v-loading="dataLoading"
              :data="dictDataList"
              size="small"
            >
              <el-table-column label="字典标签" prop="dictLabel" show-overflow-tooltip />
              <el-table-column label="字典值" prop="dictValue" show-overflow-tooltip />
              <el-table-column label="排序" prop="dictSort" width="60" />
              <el-table-column label="状态" width="60">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="默认" width="60">
                <template #default="{ row }">
                  <el-tag v-if="row.isDefault === 1" type="primary" size="small">
                    默认
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button
                    text
                    type="primary"
                    size="small"
                    @click="editDictData(row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    text
                    type="danger"
                    size="small"
                    @click="deleteDictData(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 数据分页 -->
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="dataPagination.pageNum"
                v-model:page-size="dataPagination.pageSize"
                :total="dataPagination.total"
                :page-sizes="[10, 20, 50]"
                layout="prev, pager, next, sizes"
                small
                @size-change="fetchDictData"
                @current-change="fetchDictData"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 字典类型弹窗 -->
    <el-dialog
      v-model="typeDialogVisible"
      :title="typeDialogTitle"
      width="500px"
      :before-close="closeTypeDialog"
    >
      <el-form
        ref="typeFormRef"
        :model="typeForm"
        :rules="typeRules"
        label-width="80px"
      >
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input
            v-model="typeForm.dictType"
            placeholder="请输入字典类型"
            :disabled="isEditType"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="typeForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="closeTypeDialog">取消</el-button>
        <el-button
          type="primary"
          :loading="typeSubmitting"
          @click="submitDictType"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 字典数据弹窗 -->
    <el-dialog
      v-model="dataDialogVisible"
      :title="dataDialogTitle"
      width="500px"
      :before-close="closeDataDialog"
    >
      <el-form
        ref="dataFormRef"
        :model="dataForm"
        :rules="dataRules"
        label-width="80px"
      >
        <el-form-item label="字典类型">
          <el-input :value="currentDictType?.dictType" disabled />
        </el-form-item>
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number
            v-model="dataForm.dictSort"
            :min="0"
            :max="999"
            placeholder="排序号"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="默认值">
          <el-radio-group v-model="dataForm.isDefault">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="样式类">
          <el-input v-model="dataForm.cssClass" placeholder="CSS样式类名" />
        </el-form-item>
        <el-form-item label="表格样式">
          <el-select v-model="dataForm.listClass" placeholder="请选择表格样式">
            <el-option label="默认" value="" />
            <el-option label="主要" value="primary" />
            <el-option label="成功" value="success" />
            <el-option label="警告" value="warning" />
            <el-option label="危险" value="danger" />
            <el-option label="信息" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="dataForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="closeDataDialog">取消</el-button>
        <el-button
          type="primary"
          :loading="dataSubmitting"
          @click="submitDictData"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getDictTypeList,
  createDictType,
  updateDictType,
  deleteDictType as deleteDictTypeApi,
  getDictDataList,
  createDictData,
  updateDictData,
  deleteDictData as deleteDictDataApi
} from '@/api/dict'
import type { DictType, DictData } from '@/types/api'
import { Plus, Search } from '@element-plus/icons-vue'

// 响应式数据
const typeLoading = ref(false)
const dataLoading = ref(false)
const dictTypeList = ref<DictType[]>([])
const dictDataList = ref<DictData[]>([])
const currentDictType = ref<DictType | null>(null)

// 弹窗相关
const typeDialogVisible = ref(false)
const dataDialogVisible = ref(false)
const isEditType = ref(false)
const isEditData = ref(false)
const typeSubmitting = ref(false)
const dataSubmitting = ref(false)

// 搜索表单
const typeSearchForm = reactive({
  dictName: '',
  dictType: ''
})

const dataSearchForm = reactive({
  dictLabel: ''
})

// 分页信息
const typePagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const dataPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 表单相关
const typeFormRef = ref<FormInstance>()
const dataFormRef = ref<FormInstance>()

const typeForm = reactive<Partial<DictType>>({
  dictName: '',
  dictType: '',
  status: 1,
  remark: ''
})

const dataForm = reactive<Partial<DictData>>({
  dictType: '',
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  status: 1,
  isDefault: 0,
  cssClass: '',
  listClass: '',
  remark: ''
})

// 表单验证规则
const typeRules: FormRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

const dataRules: FormRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

// 计算属性
const typeDialogTitle = computed(() => isEditType.value ? '编辑字典类型' : '新增字典类型')
const dataDialogTitle = computed(() => isEditData.value ? '编辑字典数据' : '新增字典数据')

// 获取字典类型列表
const fetchDictTypes = async () => {
  typeLoading.value = true
  try {
    const { data } = await getDictTypeList({
      pageNum: typePagination.pageNum,
      pageSize: typePagination.pageSize,
      dictName: typeSearchForm.dictName,
      dictType: typeSearchForm.dictType
    })
    dictTypeList.value = data.records
    typePagination.total = data.total
  } catch (error) {
    console.error('获取字典类型失败:', error)
  } finally {
    typeLoading.value = false
  }
}

// 获取字典数据列表
const fetchDictData = async () => {
  if (!currentDictType.value) return
  
  dataLoading.value = true
  try {
    const { data } = await getDictDataList({
      pageNum: dataPagination.pageNum,
      pageSize: dataPagination.pageSize,
      dictType: currentDictType.value.dictType,
      dictLabel: dataSearchForm.dictLabel
    })
    dictDataList.value = data.records
    dataPagination.total = data.total
  } catch (error) {
    console.error('获取字典数据失败:', error)
  } finally {
    dataLoading.value = false
  }
}

// 搜索字典类型
const searchDictTypes = () => {
  typePagination.pageNum = 1
  fetchDictTypes()
}

// 搜索字典数据
const searchDictData = () => {
  dataPagination.pageNum = 1
  fetchDictData()
}

// 选择字典类型
const handleTypeSelect = (type: DictType | null) => {
  currentDictType.value = type
  if (type) {
    dataPagination.pageNum = 1
    dataSearchForm.dictLabel = ''
    fetchDictData()
  } else {
    dictDataList.value = []
  }
}

// 显示新增类型弹窗
const showAddTypeDialog = () => {
  isEditType.value = false
  resetTypeForm()
  typeDialogVisible.value = true
}

// 编辑字典类型
const editDictType = (type: DictType) => {
  isEditType.value = true
  Object.assign(typeForm, type)
  typeDialogVisible.value = true
}

// 删除字典类型
const deleteDictType = (type: DictType) => {
  ElMessageBox.confirm(
    `确定要删除字典类型"${type.dictName}"吗？`,
    '确认删除',
    { type: 'warning' }
  ).then(async () => {
    try {
      await deleteDictTypeApi(type.id)
      ElMessage.success('删除成功')
      fetchDictTypes()
      if (currentDictType.value?.id === type.id) {
        currentDictType.value = null
        dictDataList.value = []
      }
    } catch (error) {
      console.error('删除字典类型失败:', error)
    }
  })
}

// 显示新增数据弹窗
const showAddDataDialog = () => {
  if (!currentDictType.value) return
  
  isEditData.value = false
  resetDataForm()
  dataForm.dictType = currentDictType.value.dictType
  dataDialogVisible.value = true
}

// 编辑字典数据
const editDictData = (data: DictData) => {
  isEditData.value = true
  Object.assign(dataForm, data)
  dataDialogVisible.value = true
}

// 删除字典数据
const deleteDictData = (data: DictData) => {
  ElMessageBox.confirm(
    `确定要删除字典数据"${data.dictLabel}"吗？`,
    '确认删除',
    { type: 'warning' }
  ).then(async () => {
    try {
      await deleteDictDataApi(data.id)
      ElMessage.success('删除成功')
      fetchDictData()
    } catch (error) {
      console.error('删除字典数据失败:', error)
    }
  })
}

// 提交字典类型
const submitDictType = async () => {
  if (!typeFormRef.value) return

  try {
    await typeFormRef.value.validate()
    typeSubmitting.value = true

    if (isEditType.value) {
      await updateDictType(typeForm as DictType)
      ElMessage.success('更新成功')
    } else {
      await createDictType(typeForm)
      ElMessage.success('创建成功')
    }

    closeTypeDialog()
    fetchDictTypes()
  } catch (error) {
    console.error('保存字典类型失败:', error)
  } finally {
    typeSubmitting.value = false
  }
}

// 提交字典数据
const submitDictData = async () => {
  if (!dataFormRef.value) return

  try {
    await dataFormRef.value.validate()
    dataSubmitting.value = true

    if (isEditData.value) {
      await updateDictData(dataForm as DictData)
      ElMessage.success('更新成功')
    } else {
      await createDictData(dataForm)
      ElMessage.success('创建成功')
    }

    closeDataDialog()
    fetchDictData()
  } catch (error) {
    console.error('保存字典数据失败:', error)
  } finally {
    dataSubmitting.value = false
  }
}

// 关闭类型弹窗
const closeTypeDialog = () => {
  typeDialogVisible.value = false
  nextTick(() => resetTypeForm())
}

// 关闭数据弹窗
const closeDataDialog = () => {
  dataDialogVisible.value = false
  nextTick(() => resetDataForm())
}

// 重置类型表单
const resetTypeForm = () => {
  if (typeFormRef.value) {
    typeFormRef.value.resetFields()
  }
  Object.assign(typeForm, {
    id: undefined,
    dictName: '',
    dictType: '',
    status: 1,
    remark: ''
  })
}

// 重置数据表单
const resetDataForm = () => {
  if (dataFormRef.value) {
    dataFormRef.value.resetFields()
  }
  Object.assign(dataForm, {
    id: undefined,
    dictType: '',
    dictLabel: '',
    dictValue: '',
    dictSort: 0,
    status: 1,
    isDefault: 0,
    cssClass: '',
    listClass: '',
    remark: ''
  })
}

// 页面加载时获取数据
onMounted(() => {
  fetchDictTypes()
})
</script>

<style lang="scss" scoped>
.dict-type-card,
.dict-data-card {
  height: calc(100vh - 200px);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .current-type {
      font-size: 12px;
      color: #666;
    }
  }
  
  :deep(.el-card__body) {
    height: calc(100% - 60px);
    display: flex;
    flex-direction: column;
    
    .el-table {
      flex: 1;
    }
  }
}

.pagination-wrapper {
  margin-top: 16px;
  text-align: center;
}

.empty-state {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 响应式设计
@media (max-width: 1200px) {
  .dict-type-card,
  .dict-data-card {
    height: auto;
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .el-dialog {
    width: 95% !important;
    margin: 0 auto;
  }
  
  .el-table {
    font-size: 12px;
  }
}
</style>