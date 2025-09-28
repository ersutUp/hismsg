import { request } from '@/utils/request'
import type { PageResponse, DictType, DictData } from '@/types/api'

// 字典类型相关API
export const getDictTypeList = (params: {
  pageNum?: number
  pageSize?: number
  dictName?: string
  dictType?: string
}) => {
  return request.get<PageResponse<DictType>>('/dict/type/list', { params })
}

export const getDictTypeDetail = (id: number) => {
  return request.get<DictType>(`/dict/type/${id}`)
}

export const createDictType = (data: Partial<DictType>) => {
  return request.post('/dict/type', data)
}

export const updateDictType = (data: DictType) => {
  return request.put('/dict/type', data)
}

export const deleteDictType = (id: number) => {
  return request.delete(`/dict/type/${id}`)
}

// 字典数据相关API
export const getDictDataList = (params: {
  pageNum?: number
  pageSize?: number
  dictType?: string
  dictLabel?: string
}) => {
  return request.get<PageResponse<DictData>>('/dict/data/list', { params })
}

export const getDictDataByType = (dictType: string) => {
  return request.get<DictData[]>(`/dict/data/type/${dictType}`)
}

export const getDictDataDetail = (id: number) => {
  return request.get<DictData>(`/dict/data/${id}`)
}

export const createDictData = (data: Partial<DictData>) => {
  return request.post('/dict/data', data)
}

export const updateDictData = (data: DictData) => {
  return request.put('/dict/data', data)
}

export const deleteDictData = (id: number) => {
  return request.delete(`/dict/data/${id}`)
}

export const getDictLabel = (dictType: string, dictValue: string) => {
  return request.get<string>(`/dict/data/label?dictType=${dictType}&dictValue=${dictValue}`)
}