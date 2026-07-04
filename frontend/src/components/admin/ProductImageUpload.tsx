'use client';

import { useRef, useState } from 'react';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import { mediaService } from '@/lib/services/mediaService';

interface ProductImageUploadProps {
  imageUrl: string;
  onImageUrlChange: (url: string) => void;
}

const MAX_IMAGE_SIZE = 5 * 1024 * 1024;
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

export default function ProductImageUpload({ imageUrl, onImageUrlChange }: ProductImageUploadProps) {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');

  const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
      setMessage('Only jpg, png, webp, and gif images are allowed.');
      event.target.value = '';
      return;
    }

    if (file.size > MAX_IMAGE_SIZE) {
      setMessage('Image file must be 5MB or smaller.');
      event.target.value = '';
      return;
    }

    setUploading(true);
    setMessage('');
    try {
      const uploaded = await mediaService.uploadProductImage(file);
      onImageUrlChange(uploaded.url);
      setMessage('Image uploaded.');
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Image upload failed.');
    } finally {
      setUploading(false);
      event.target.value = '';
    }
  };

  return (
    <div className="space-y-3">
      <Input
        label="Image URL"
        value={imageUrl}
        onChange={(e) => onImageUrlChange(e.target.value)}
        placeholder="https://example.com/image.jpg"
        fullWidth
      />

      <div className="flex flex-wrap items-center gap-2">
        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp,image/gif"
          onChange={handleFileChange}
          className="hidden"
        />
        <Button
          variant="outline"
          type="button"
          disabled={uploading}
          onClick={() => fileInputRef.current?.click()}
        >
          {uploading ? 'Uploading...' : 'Upload image'}
        </Button>
        {imageUrl && (
          <Button
            variant="ghost"
            type="button"
            disabled={uploading}
            onClick={() => onImageUrlChange('')}
            className="text-[#d94f4f]"
          >
            Clear image
          </Button>
        )}
      </div>

      {message && (
        <p className="text-xs text-[#6b7280]">{message}</p>
      )}

      {imageUrl ? (
        <div className="w-36 overflow-hidden border border-[#e8eaf0] bg-[#f7f8fc] aspect-[4/5]">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={imageUrl} alt="Product preview" className="h-full w-full object-cover" />
        </div>
      ) : (
        <div className="flex h-24 w-36 items-center justify-center border border-dashed border-[#d8dce6] bg-[#f7f8fc] text-xs text-[#8a9bb5]">
          No image
        </div>
      )}
    </div>
  );
}
