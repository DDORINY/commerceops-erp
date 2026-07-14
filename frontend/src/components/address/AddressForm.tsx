'use client';
import { useState } from 'react';
import PostcodeSearch from './PostcodeSearch';
import type { Address, AddressInput } from '@/lib/services/addressService';
const empty:AddressInput={addressName:'',recipientName:'',phone:'',postalCode:'',roadAddress:'',detailAddress:'',extraAddress:'',deliveryRequest:'',isDefault:false};
export default function AddressForm({initial,onSubmit,onCancel,submitLabel='저장'}:{initial?:Address|AddressInput;onSubmit:(data:AddressInput)=>Promise<void>;onCancel?:()=>void;submitLabel?:string}) {
 const [form,setForm]=useState<AddressInput>(initial?{addressName:initial.addressName,recipientName:initial.recipientName,phone:initial.phone,postalCode:initial.postalCode,roadAddress:initial.roadAddress,detailAddress:initial.detailAddress??'',extraAddress:initial.extraAddress??'',deliveryRequest:initial.deliveryRequest??'',isDefault:initial.isDefault}:empty);
 const [error,setError]=useState(''); const [saving,setSaving]=useState(false);
 const field=(key:keyof AddressInput,value:string|boolean)=>setForm(p=>({...p,[key]:value}));
 const submit=async(e:React.FormEvent)=>{e.preventDefault();if(!form.addressName.trim()||!form.recipientName.trim()||!/^[-+() 0-9]{8,20}$/.test(form.phone)||!/^\d{5,6}$/.test(form.postalCode)||!form.roadAddress.trim()){setError('필수 항목과 연락처, 우편번호 형식을 확인해주세요.');return;}setSaving(true);setError('');try{await onSubmit(form);}catch(err){setError(err instanceof Error?err.message:'저장에 실패했습니다.');}finally{setSaving(false);}};
 const input='w-full border border-[#ddd] px-3 py-2 text-sm outline-none focus:border-[#222]';
 return <form onSubmit={submit} className="space-y-3">
  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3"><input className={input} placeholder="배송지명 (집, 회사)" value={form.addressName} onChange={e=>field('addressName',e.target.value)}/><input className={input} placeholder="수령인" value={form.recipientName} onChange={e=>field('recipientName',e.target.value)}/></div>
  <input className={input} placeholder="연락처" value={form.phone} onChange={e=>field('phone',e.target.value)}/>
  <div className="flex gap-2"><input className={input} placeholder="우편번호" value={form.postalCode} onChange={e=>field('postalCode',e.target.value)}/><PostcodeSearch onSelect={(z,r,x)=>setForm(p=>({...p,postalCode:z,roadAddress:r,extraAddress:x}))}/></div>
  <input className={input} placeholder="도로명 기본주소 (직접 입력 가능)" value={form.roadAddress} onChange={e=>field('roadAddress',e.target.value)}/>
  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3"><input className={input} placeholder="상세주소" value={form.detailAddress??''} onChange={e=>field('detailAddress',e.target.value)}/><input className={input} placeholder="참고/추가주소" value={form.extraAddress??''} onChange={e=>field('extraAddress',e.target.value)}/></div>
  <input className={input} placeholder="배송 요청사항" value={form.deliveryRequest??''} onChange={e=>field('deliveryRequest',e.target.value)}/>
  <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.isDefault} onChange={e=>field('isDefault',e.target.checked)}/> 기본 배송지로 설정</label>
  {error&&<p className="text-sm text-red-600">{error}</p>}<div className="flex justify-end gap-2">{onCancel&&<button type="button" onClick={onCancel} className="border px-4 py-2 text-sm">취소</button>}<button disabled={saving} className="bg-[#222] px-5 py-2 text-sm text-white disabled:opacity-50">{saving?'저장 중...':submitLabel}</button></div>
 </form>;
}
