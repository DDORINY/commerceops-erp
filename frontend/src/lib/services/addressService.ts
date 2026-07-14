import { apiClient } from '@/lib/api';
export interface Address { addressId:number; addressName:string; recipientName:string; phone:string; postalCode:string; roadAddress:string; detailAddress:string|null; extraAddress:string|null; deliveryRequest:string|null; isDefault:boolean; createdAt:string; updatedAt:string }
export type AddressInput = Omit<Address,'addressId'|'createdAt'|'updatedAt'>;
export const addressService = {
 list: () => apiClient<Address[]>('/addresses'),
 create: (data:AddressInput) => apiClient<Address>('/addresses',{method:'POST',body:JSON.stringify(data)}),
 update: (id:number,data:AddressInput) => apiClient<Address>(`/addresses/${id}`,{method:'PUT',body:JSON.stringify(data)}),
 remove: (id:number) => apiClient<void>(`/addresses/${id}`,{method:'DELETE'}),
 setDefault: (id:number) => apiClient<Address>(`/addresses/${id}/default`,{method:'PATCH'}),
};
