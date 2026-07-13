'use client';

import { useState } from 'react';

type ImageResult = {
  modelName: string;
  filename: string | null;
  width: number;
  height: number;
  quality: string;
  brightness: number;
};

export default function AiImageInferenceCard() {
  const [result, setResult] = useState<ImageResult | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(file: File | undefined) {
    if (!file) return;
    if (file.size === 0 || file.size > 5 * 1024 * 1024) {
      setError("이미지는 비어 있지 않아야 하며 5MB 이하여야 합니다.");
      return;
    }
    setLoading(true);
    setError('');
    setResult(null);
    try {
      const body = new FormData();
      body.append('file', file);
      const controller = new AbortController();
      const timeout = window.setTimeout(() => controller.abort(), 30000);
      let response;
      try {
        response = await fetch('/ai/predict/image', { method: 'POST', body, signal: controller.signal });
      } finally {
        window.clearTimeout(timeout);
      }
      const payload = await response.json();
      if (!response.ok) throw new Error(payload.detail || '이미지 추론에 실패했습니다.');
      setResult(payload as ImageResult);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : '이미지 추론에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="border border-[#e8eaf0] bg-white p-5">
      <h2 className="text-base font-semibold text-[#1a1f2e]">이미지 추론 점검</h2>
      <p className="mt-2 text-sm text-[#6f7a8a]">현재 이미지 분석 기능은 학습된 비전 모델이 아닌 밝기와 해상도 기반의 실험용 baseline 분석입니다.</p>
      <input className="mt-4 block text-sm" type="file" accept="image/jpeg,image/png,image/webp,image/gif" onChange={(event) => submit(event.target.files?.[0])} />
      {loading && <p className="mt-3 text-sm text-[#6f7a8a]">추론 중입니다.</p>}
      {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
      {result && <div className="mt-4 grid gap-2 text-sm text-[#4b5565]"><p>모델: {result.modelName}</p><p>파일: {result.filename}</p><p>크기: {result.width} × {result.height}</p><p>결과: {result.quality} (밝기 {result.brightness})</p></div>}
    </section>
  );
}
