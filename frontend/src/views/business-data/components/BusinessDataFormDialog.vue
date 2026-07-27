<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑数据' : '新增数据'"
    width="680px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
    @closed="handleClosed"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      label-position="right"
    >
      <el-form-item
        v-for="field in sortedFields"
        :key="field.fieldCode"
        :label="field.fieldName"
        :prop="field.fieldCode"
      >
        <el-input
          v-if="field.fieldType === 'TEXT'"
          v-model="formData[field.fieldCode]"
          :placeholder="`请输入${field.fieldName}`"
          maxlength="500"
          clearable
        />
        <el-input-number
          v-else-if="field.fieldType === 'NUMBER'"
          v-model="formData[field.fieldCode]"
          :placeholder="`请输入${field.fieldName}`"
          controls-position="right"
          style="width: 100%"
        />
        <el-date-picker
          v-else-if="field.fieldType === 'DATE'"
          v-model="formData[field.fieldCode]"
          type="date"
          :placeholder="`请选择${field.fieldName}`"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
        <el-select
          v-else-if="field.fieldType === 'SELECT'"
          v-model="formData[field.fieldCode]"
          :placeholder="`请选择${field.fieldName}`"
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="opt in field.selectOptions || []"
            :key="opt"
            :label="opt"
            :value="opt"
          />
        </el-select>
        <el-select
          v-else-if="field.fieldType === 'REFERENCE'"
          v-model="formData[field.fieldCode]"
          :placeholder="`请选择${field.fieldName}`"
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="item in referenceDataMap[field.fieldCode] || []"
            :key="item.id"
            :label="getReferenceLabel(item)"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getBusinessDataDetail, createBusinessData, updateBusinessData, getReferenceModelData, checkBusinessDataUnique } from '@/api/businessData'
import type { DataModel, DataModelField, BusinessDataItem } from '@/types'

const props = defineProps<{
  visible: boolean
  model: DataModel | null
  dataId: number | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const formData = ref<Record<string, any>>({})
const referenceDataMap = ref<Record<string, BusinessDataItem[]>>({})

const isEdit = computed(() => props.dataId !== null)

const sortedFields = computed(() => {
  if (!props.model) return []
  return [...props.model.fields].sort((a, b) => a.sort - b.sort)
})

const formRules = computed<FormRules>(() => {
  if (!props.model) return {}
  const rules: FormRules = {}
  for (const field of props.model.fields) {
    const fieldRules: any[] = []
    if (field.required) {
      fieldRules.push({
        required: true,
        message: `请${field.fieldType === 'SELECT' || field.fieldType === 'REFERENCE' || field.fieldType === 'DATE' ? '选择' : '输入'}${field.fieldName}`,
        trigger: field.fieldType === 'TEXT' ? 'blur' : 'change',
      })
    }
    if (field.unique) {
      fieldRules.push({
        validator: createUniqueValidator(field),
        trigger: 'blur',
      })
    }
    if (fieldRules.length > 0) {
      rules[field.fieldCode] = fieldRules
    }
  }
  return rules
})

function createUniqueValidator(field: DataModelField) {
  return async (_rule: any, value: any, callback: (err?: Error) => void) => {
    if (!value) {
      callback()
      return
    }
    try {
      const { data } = await checkBusinessDataUnique(
        props.model!.id,
        field.fieldCode,
        value,
        isEdit.value ? props.dataId! : undefined
      )
      if (data) {
        callback()
      } else {
        callback(new Error(`${field.fieldName}已存在`))
      }
    } catch {
      callback()
    }
  }
}

watch(
  () => props.visible,
  async (val) => {
    if (val && props.model) {
      await loadReferenceData()
      if (isEdit.value && props.dataId) {
        await fetchDetail(props.dataId)
      } else {
        initFormData()
      }
    }
  }
)

async function loadReferenceData() {
  if (!props.model) return
  const refFields = props.model.fields.filter((f) => f.fieldType === 'REFERENCE' && f.referenceModelId)
  for (const field of refFields) {
    try {
      const { data } = await getReferenceModelData(field.referenceModelId!)
      referenceDataMap.value[field.fieldCode] = data
    } catch {
      referenceDataMap.value[field.fieldCode] = []
    }
  }
}

async function fetchDetail(id: number) {
  try {
    const { data } = await getBusinessDataDetail(props.model!.id, id)
    formData.value = { ...data.data }
  } catch {
    ElMessage.error('获取数据详情失败')
  }
}

function initFormData() {
  const data: Record<string, any> = {}
  if (props.model) {
    for (const field of props.model.fields) {
      data[field.fieldCode] = field.fieldType === 'NUMBER' ? undefined : null
    }
  }
  formData.value = data
}

function getReferenceLabel(item: BusinessDataItem) {
  const data = item.data
  const keys = Object.keys(data)
  if (keys.length > 0) {
    return `${item.id} - ${Object.values(data).find((v) => v !== null && v !== undefined) || item.id}`
  }
  return String(item.id)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = { ...formData.value }
    for (const field of props.model!.fields) {
      if (submitData[field.fieldCode] === null || submitData[field.fieldCode] === undefined || submitData[field.fieldCode] === '') {
        delete submitData[field.fieldCode]
      }
    }
    if (isEdit.value && props.dataId) {
      await updateBusinessData(props.dataId, props.model!.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createBusinessData(props.model!.id, submitData)
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
  formData.value = {}
  referenceDataMap.value = {}
}
</script>
