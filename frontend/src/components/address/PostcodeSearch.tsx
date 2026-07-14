'use client';
declare global { interface Window { daum?: { Postcode: new (options:{oncomplete:(data:{zonecode:string;roadAddress:string;bname:string;buildingName:string})=>void}) => {open:()=>void} } } }
export default function PostcodeSearch({ onSelect }: { onSelect:(postalCode:string,roadAddress:string,extraAddress:string)=>void }) {
 const open = () => {
  const launch = () => new window.daum!.Postcode({oncomplete:(d) => onSelect(d.zonecode,d.roadAddress,[d.bname,d.buildingName].filter(Boolean).join(', '))}).open();
  if (window.daum?.Postcode) { launch(); return; }
  const script=document.createElement('script'); script.src='https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js'; script.onload=launch; script.onerror=()=>alert('주소 검색을 불러오지 못했습니다. 직접 입력해주세요.'); document.head.appendChild(script);
 };
 return <button type="button" onClick={open} className="shrink-0 border border-[#555] px-4 py-2 text-sm hover:bg-[#f5f5f5]">주소 검색</button>;
}
