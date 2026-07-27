<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑数据模型' : '新增数据模型'"
    width="900px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
    @closed="handleClosed"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="formRules"
      label-width="100px"
      label-position="right"
    >
      <el-form-item label="模型编码" prop="modelCode">
        <el-input
          v-model="form.modelCode"
          placeholder="请输入模型编码"
          :disabled="isEdit"
          maxlength="50"
        />
      </el-form-item>
      <el-form-item label="模型名称" prop="modelName">
        <el-input v-model="form.modelName" placeholder="请输入模型名称" maxlength="100" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请输入描述"
          :rows="2"
          maxlength="500"
        />
      </el-form-item>
      <el-form-item label="表名" prop="tableName">
        <el-input
          v-model="form.tableName"
          placeholder="请输入表名"
          :disabled="isEdit"
          maxlength="100"
        />
      </el-form-item>

      <el-divider content-position="left">字段管理</el-divider>

      <div class="fields-header">
        <el-button type="primary" size="small" @click="addField">添加字段</el-button>
      </div>

      <div class="fields-list">
        <div v-for="(field, index) in form.fields" :key="index" class="field-row">
          <el-form-item
            :label="`字段 ${index + 1}`"
            :prop="`fields.${index}.fieldCode`"
            :rules="fieldRules.fieldCode"
            class="field-form-item"
          >
            <div class="field-row-inner">
              <el-input v-model="field.fieldCode" placeholder="字段编码" style="width: 120px" />
              <el-input v-model="field.fieldName" placeholder="字段名称" style="width: 120px" />
              <el-select v-model="field.fieldType" placeholder="字段类型" style="width: 130px" @change="handleFieldTypeChange(field)">
                <el-option label="文本" value="TEXT" />
                <el-option label="数字" value="NUMBER" />
                <el-option label="日期" value="DATE" />
                <el-option label="选择" value="SELECT" />
                <el-option label="引用" value="REFERENCE" />
              </el-select>
              <el-checkbox v-model="field.required" label="必填" />
              <el-checkbox v-model="field.unique" label="唯一" />
              <el-select
                v-if="field.fieldType === 'REFERENCE'"
                v-model="field.referenceModelId"
                placeholder="引用模型"
                style="width: 140px"
                clearable
              >
                <el-option
                  v-for="m in modelList"
                  :key="m.id"
                  :label="m.modelName"
                  :value="m.id"
                />
              </el-select>
              <el-input-number
                v-model="field.sort"
                :min="0"
                :max="999"
                controls-position="right"
                style="width: 100px"
              />
              <el-button link type="danger" @click="removeField(index)">删除</el-button>
            </div>
          </el-form-item>
          <div v-if="field.fieldType === 'SELECT'" class="select-options-row">
            <el-form-item
              :prop="`fields.${index}.selectOptions`"
              :rules="fieldRules.selectOptions"
              label="选项列表"
              label-width="100px"
              style="margin-bottom: 0"
            >
              <div class="select-options-wrapper">
                <el-tag
                  v-for="(opt, optIdx) in field.selectOptions"
                  :key="optIdx"
                  closable
                  style="margin-right: 6px; margin-bottom: 4px"
                  @close="removeSelectOption(field, optIdx)"
                >
                  {{ opt }}
                </el-tag>
                <el-input
                  v-if="optionInputVisible[`${index}`]"
                  :ref="(el: any) => setOptionInputRef(index, el)"
                  v-model="optionInputValue"
                  size="small"
                  style="width: 100px"
                  @keyup.enter="handleOptionConfirm(field, index)"
                  @blur="handleOptionConfirm(field, index)"
                />
                <el-button v-else size="small" @click="showOptionInput(index)">
                  + 添加选项
                </el-button>
              </div>
            </el-form-item>
          </div>
        </div>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getDataModelDetail, createDataModel, updateDataModel, getDataModelList } from '@/api/dataModel'
import type { DataModel, DataModelField, FieldType } from '@/types'

const props = defineProps<{
  visible: boolean
  modelId: number | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const modelList = ref<DataModel[]>([])
const optionInputVisible = ref<Record<string, boolean>>({})
const optionInputValue = ref('')
const optionInputRefs = ref<Record<number, any>>({})

const isEdit = computed(() => props.modelId !== null)

const defaultField = (): DataModelField => ({
  fieldCode: '',
  fieldName: '',
  fieldType: 'TEXT' as FieldType,
  required: false,
  unique: false,
  referenceModelId: null,
  selectOptions: [],
  sort: 0,
})

const form = reactive({
  modelCode: '',
  modelName: '',
  description: '',
  tableName: '',
  fields: [defaultField()] as DataModelField[],
})

const formRules: FormRules = {
  modelCode: [
    { required: true, message: '请输入模型编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '编码需以字母开头，仅含字母数字下划线', trigger: 'blur' },
  ],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  tableName: [
    { required: true, message: '请输入表名', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '表名需以字母开头，仅含字母数字下划线', trigger: 'blur' },
  ],
}

const fieldRules: FormRules = {
  fieldCode: [
    { required: true, message: '请输入字段编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '编码需以字母开头', trigger: 'blur' },
  ],
  selectOptions: [
    {
      validator: (_rule: any, value: string[], callback: (err?: Error) => void) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少添加一个选项'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

watch(
  () => props.visible,
  async (val) => {
    if (val) {
      await fetchModelList()
      if (props.modelId) {
        await fetchModelDetail(props.modelId)
      } else {
        resetForm()
      }
    }
  }
)

async function fetchModelList() {
  try {
    const { data } = await getDataModelList()
    modelList.value = data
  } catch {
    modelList.value = []
  }
}

async function fetchModelDetail(id: number) {
  try {
    const { data } = await getDataModelDetail(id)
    form.modelCode = data.modelCode
    form.modelName = data.modelName
    form.description = data.description
    form.tableName = data.tableName
    form.fields = data.fields && data.fields.length > 0
      ? data.fields.map((f) => ({ ...f, selectOptions: f.selectOptions || [] }))
      : [defaultField()]
  } catch {
    ElMessage.error('获取模型详情失败')
  }
}

function resetForm() {
  form.modelCode = ''
  form.modelName = ''
  form.description = ''
  form.tableName = ''
  form.fields = [defaultField()]
}

function addField() {
  form.fields.push(defaultField())
}

function removeField(index: number) {
  form.fields.splice(index, 1)
}

function handleFieldTypeChange(field: DataModelField) {
  if (field.fieldType !== 'REFERENCE') {
    field.referenceModelId = null
  }
  if (field.fieldType !== 'SELECT') {
    field.selectOptions = []
  } else if (!field.selectOptions) {
    field.selectOptions = []
  }
}

function setOptionInputRef(index: number, el: any) {
  optionInputRefs.value[index] = el
}

function showOptionInput(index: number) {
  optionInputVisible.value[`${index}`] = true
  optionInputValue.value = ''
  nextTick(() => {
    optionInputRefs.value[index]?.focus()
  })
}

function handleOptionConfirm(field: DataModelField, index: number) {
  const val = optionInputValue.value.trim()
  if (val) {
    if (!field.selectOptions) {
      field.selectOptions = []
    }
    if (field.selectOptions.includes(val)) {
      ElMessage.warning('选项已存在')
    } else {
      field.selectOptions.push(val)
    }
  }
  optionInputVisible.value[`${index}`] = false
  optionInputValue.value = ''
}

function removeSelectOption(field: DataModelField, optIdx: number) {
  field.selectOptions?.splice(optIdx, 1)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const fieldCodes = form.fields.map((f) => f.fieldCode)
  const duplicateCodes = fieldCodes.filter((c, i) => fieldCodes.indexOf(c) !== i)
  if (duplicateCodes.length > 0) {
    ElMessage.warning(`字段编码重复: ${[...new Set(duplicateCodes)].join(', ')}`)
    return
  }

  submitLoading.value = true
  try {
    const payload = {
      modelCode: form.modelCode,
      modelName: form.modelName,
      description: form.description,
      tableName: form.tableName,
      fields: form.fields.map((f, i) => ({ ...f, sort: f.sort || i })),
    }
    if (isEdit.value && props.modelId) {
      await updateDataModel(props.modelId, payload)
      ElMessage.success('更新成功')
    } else {
      await createDataModel(payload)
      ElMessage.success('创建成功')
    }
    emit('update:visible', false)
    emit('success')
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitLoading.value = false
  }
}

function handleClosed() {
  formRef.value?.resetFields()
  resetForm()
  optionInputVisible.value = {}
  optionInputValue.value = ''
}
</script>

<style scoped>
.fields-header {
  margin-bottom: 12px;
}

.fields-list {
  max-height: 400px;
  overflow-y: auto;
}

.field-row {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px 12px 4px;
  margin-bottom: 12px;
  background: #fafbfc;
}

.field-form-item {
  margin-bottom: 8px;
}

.field-form-item :deep(.el-form-item__content) {
  display: block;
}

.field-row-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.select-options-row {
  padding: 0 0 8px 0;
}

.select-options-wrapper {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
