<template>
  <el-dialog
    :model-value="visible"
    title="数据关联关系"
    width="800px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
  >
    <div v-loading="loading">
      <div v-if="detail" class="reference-content">
        <div class="section">
          <h4 class="section-title">当前数据</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item
              v-for="field in currentModelFields"
              :key="field.fieldCode"
              :label="field.fieldName"
            >
              {{ detail.currentData.data[field.fieldCode] ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ detail.currentData.createdAt }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <h4 class="section-title">引用该数据的其他数据</h4>
          <el-table
            v-if="detail.referencedBy.length > 0"
            :data="detail.referencedBy"
            stripe
            border
            size="small"
          >
            <el-table-column prop="sourceModelName" label="来源模型" width="150" />
            <el-table-column prop="sourceFieldCode" label="引用字段" width="150" />
            <el-table-column prop="sourceDataLabel" label="来源数据" min-width="200" show-overflow-tooltip />
            <el-table-column prop="sourceDataId" label="数据ID" width="100" />
          </el-table>
          <el-empty v-else description="暂无其他数据引用" :image-size="60" />
        </div>

        <div class="section">
          <h4 class="section-title">该数据引用的其他数据</h4>
          <el-table
            v-if="detail.references.length > 0"
            :data="detail.references"
            stripe
            border
            size="small"
          >
            <el-table-column prop="sourceModelName" label="目标模型" width="150" />
            <el-table-column prop="sourceFieldCode" label="引用字段" width="150" />
            <el-table-column prop="sourceDataLabel" label="目标数据" min-width="200" show-overflow-tooltip />
            <el-table-column prop="sourceDataId" label="数据ID" width="100" />
          </el-table>
          <el-empty v-else description="暂无引用其他数据" :image-size="60" />
        </div>
      </div>
      <el-empty v-else description="暂无关联数据" />
    </div>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDataModelDetail } from '@/api/dataModel'
import { getDataReferences } from '@/api/businessData'
import type { DataModel, DataModelField, DataReferenceDetail } from '@/types'

const props = defineProps<{
  visible: boolean
  modelId: number | null
  dataId: number | null
}>()

defineEmits<{
  (e: 'update:visible', val: boolean): void
}>()

const loading = ref(false)
const detail = ref<DataReferenceDetail | null>(null)
const currentModelFields = ref<DataModelField[]>([])

watch(
  () => props.visible,
  async (val) => {
    if (val && props.modelId && props.dataId) {
      await fetchReferences()
      await fetchModelFields()
    } else {
      detail.value = null
      currentModelFields.value = []
    }
  }
)

async function fetchReferences() {
  if (!props.modelId || !props.dataId) return
  loading.value = true
  try {
    const { data } = await getDataReferences(props.modelId, props.dataId)
    detail.value = data
  } catch {
    ElMessage.error('获取关联数据失败')
    detail.value = null
  } finally {
    loading.value = false
  }
}

async function fetchModelFields() {
  if (!props.modelId) return
  try {
    const { data } = await getDataModelDetail(props.modelId)
    currentModelFields.value = data.fields || []
  } catch {
    currentModelFields.value = []
  }
}
</script>

<style scoped>
.reference-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}
</style>
