/**
 * 业务数据管理 API 模块
 * 提供业务数据的增删改查、批量删除、关联查询、引用数据获取、唯一性校验等接口
 */
import request from '@/utils/request'
import type { Result, PageResult, BusinessDataItem, BusinessDataPageRequest, DataReferenceDetail } from '@/types'

/**
 * 获取业务数据分页列表
 * @param params 查询参数（含模型ID、搜索条件、分页信息）
 * @returns 业务数据分页数据
 */
export function getBusinessDataPage(params: BusinessDataPageRequest) {
  return request.get<Result<PageResult<BusinessDataItem>>>('/business-data', { params }) as unknown as Promise<Result<PageResult<BusinessDataItem>>>
}

/**
 * 获取业务数据详情
 * @param modelId 数据模型ID，用于确定数据所属模型
 * @param id 业务数据ID
 * @returns 业务数据详细信息
 */
export function getBusinessDataDetail(modelId: number, id: number) {
  return request.get<Result<BusinessDataItem>>(`/business-data/${id}`, { params: { modelId } }) as unknown as Promise<Result<BusinessDataItem>>
}

/**
 * 创建业务数据
 * @param modelId 数据模型ID
 * @param data 业务数据键值对（字段编码 -> 字段值）
 * @returns 新创建的业务数据信息
 */
export function createBusinessData(modelId: number, data: Record<string, any>) {
  return request.post<Result<BusinessDataItem>>('/business-data', { modelId, data }) as unknown as Promise<Result<BusinessDataItem>>
}

/**
 * 更新业务数据
 * @param id 业务数据ID
 * @param modelId 数据模型ID
 * @param data 更新的业务数据键值对
 * @returns 更新后的业务数据信息
 */
export function updateBusinessData(id: number, modelId: number, data: Record<string, any>) {
  return request.put<Result<BusinessDataItem>>(`/business-data/${id}`, { modelId, data }) as unknown as Promise<Result<BusinessDataItem>>
}

/**
 * 删除单条业务数据
 * @param id 业务数据ID
 * @returns 无返回数据
 */
export function deleteBusinessData(id: number) {
  return request.delete<Result<void>>(`/business-data/${id}`) as unknown as Promise<Result<void>>
}

/**
 * 批量删除业务数据
 * @param ids 待删除的业务数据ID数组
 * @returns 无返回数据
 */
export function batchDeleteBusinessData(ids: number[]) {
  return request.delete<Result<void>>('/business-data/batch', { data: ids }) as unknown as Promise<Result<void>>
}

/**
 * 获取数据关联关系详情
 * 查询指定数据被哪些数据引用，以及该数据引用了哪些其他数据
 * @param modelId 数据模型ID
 * @param dataId 业务数据ID
 * @returns 关联关系详情（含当前数据、被引用列表、引用列表）
 */
export function getDataReferences(modelId: number, dataId: number) {
  return request.get<Result<DataReferenceDetail>>(`/business-data/${dataId}/references`, { params: { modelId } }) as unknown as Promise<Result<DataReferenceDetail>>
}

/**
 * 获取引用模型的数据列表
 * 用于引用类型字段的下拉选择框数据源
 * @param modelId 被引用的数据模型ID
 * @returns 该模型下的所有业务数据列表
 */
export function getReferenceModelData(modelId: number) {
  return request.get<Result<BusinessDataItem[]>>('/business-data/reference-data', { params: { modelId } }) as unknown as Promise<Result<BusinessDataItem[]>>
}

/**
 * 校验业务数据字段值是否唯一
 * @param modelId 数据模型ID
 * @param fieldCode 字段编码
 * @param value 待校验的值
 * @param excludeId 排除的数据ID（编辑时排除自身）
 * @returns 是否唯一（true-唯一，false-已存在）
 */
export function checkBusinessDataUnique(modelId: number, fieldCode: string, value: string, excludeId?: number) {
  return request.get<Result<boolean>>('/business-data/check-unique', {
    params: { modelId, fieldCode, value, excludeId }
  }) as unknown as Promise<Result<boolean>>
}
