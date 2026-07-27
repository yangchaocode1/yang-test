/**
 * 数据模型管理 API 模块
 * 提供数据模型的增删改查、字段唯一性校验等接口
 */
import request from '@/utils/request'
import type { Result, PageResult, DataModel, DataModelPageRequest } from '@/types'

/**
 * 获取数据模型分页列表
 * @param params 查询参数（含关键词、状态、分页信息）
 * @returns 数据模型分页数据
 */
export function getDataModelPage(params: DataModelPageRequest) {
  return request.get<Result<PageResult<DataModel>>>('/data-models', { params }) as unknown as Promise<Result<PageResult<DataModel>>>
}

/**
 * 获取所有数据模型列表（不分页）
 * 用于业务数据页面的模型选择下拉框和字段引用配置
 * @returns 数据模型完整列表
 */
export function getDataModelList() {
  return request.get<Result<DataModel[]>>('/data-models/list') as unknown as Promise<Result<DataModel[]>>
}

/**
 * 获取数据模型详情
 * @param id 数据模型ID
 * @returns 数据模型详细信息（含字段列表）
 */
export function getDataModelDetail(id: number) {
  return request.get<Result<DataModel>>(`/data-models/${id}`) as unknown as Promise<Result<DataModel>>
}

/**
 * 创建新数据模型
 * @param data 数据模型数据（含编码、名称、表名、字段列表等）
 * @returns 新创建的数据模型信息
 */
export function createDataModel(data: Partial<DataModel>) {
  return request.post<Result<DataModel>>('/data-models', data) as unknown as Promise<Result<DataModel>>
}

/**
 * 更新数据模型信息
 * @param id 数据模型ID
 * @param data 更新的数据模型数据
 * @returns 更新后的数据模型信息
 */
export function updateDataModel(id: number, data: Partial<DataModel>) {
  return request.put<Result<DataModel>>(`/data-models/${id}`, data) as unknown as Promise<Result<DataModel>>
}

/**
 * 删除数据模型
 * @param id 数据模型ID
 * @returns 无返回数据
 */
export function deleteDataModel(id: number) {
  return request.delete<Result<void>>(`/data-models/${id}`) as unknown as Promise<Result<void>>
}

/**
 * 校验字段值是否唯一
 * 用于表单字段的唯一性校验（如编码、名称等）
 * @param modelId 数据模型ID
 * @param fieldCode 字段编码
 * @param value 待校验的值
 * @param excludeId 排除的数据ID（编辑时排除自身）
 * @returns 是否唯一（true-唯一，false-已存在）
 */
export function checkFieldUnique(modelId: number, fieldCode: string, value: string, excludeId?: number) {
  return request.get<Result<boolean>>('/data-models/check-unique', {
    params: { modelId, fieldCode, value, excludeId }
  }) as unknown as Promise<Result<boolean>>
}
