import { apiClient, publicApiClient } from '@/lib/api';

export type TermsType = 'TERMS_OF_SERVICE' | 'PRIVACY_POLICY' | 'SHIPPING_RETURN_POLICY';

export interface BusinessSettings {
  id: number | null;
  companyName: string | null;
  representativeName: string | null;
  businessRegistrationNumber: string | null;
  mailOrderBusinessNumber: string | null;
  address: string | null;
  customerServicePhone: string | null;
  customerServiceEmail: string | null;
  brandName: string | null;
  updatedBy: number | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface BusinessSettingsUpdateRequest {
  companyName: string;
  representativeName: string;
  businessRegistrationNumber: string;
  mailOrderBusinessNumber: string;
  address: string;
  customerServicePhone: string;
  customerServiceEmail: string;
  brandName: string;
}

export interface TermsVersion {
  id: number;
  type: TermsType;
  title: string;
  content: string;
  version: string;
  effectiveFrom: string;
  active: boolean;
  createdBy: number | null;
  createdAt: string;
}

export interface TermsVersionCreateRequest {
  type: TermsType;
  title: string;
  content: string;
  version?: string;
  effectiveFrom?: string;
}

export const TERMS_TYPE_LABEL: Record<TermsType, string> = {
  TERMS_OF_SERVICE: '\uC774\uC6A9\uC57D\uAD00',
  PRIVACY_POLICY: '\uAC1C\uC778\uC815\uBCF4\uCC98\uB9AC\uBC29\uCE68',
  SHIPPING_RETURN_POLICY: '\uBC30\uC1A1/\uBC18\uD488 \uC815\uCC45',
};

export const settingsService = {
  getCompanySettings: () => apiClient<BusinessSettings>('/admin/settings/company'),

  updateCompanySettings: (data: BusinessSettingsUpdateRequest) =>
    apiClient<BusinessSettings>('/admin/settings/company', {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  getTerms: () => apiClient<TermsVersion[]>('/admin/settings/terms'),

  createTerms: (data: TermsVersionCreateRequest) =>
    apiClient<TermsVersion>('/admin/settings/terms', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getLatestTerms: (type: TermsType) =>
    apiClient<TermsVersion>(`/admin/settings/terms/${type}/latest`),

  getTermsVersions: (type: TermsType) =>
    apiClient<TermsVersion[]>(`/admin/settings/terms/${type}/versions`),

  getTermsVersion: (type: TermsType, versionId: number) =>
    apiClient<TermsVersion>(`/admin/settings/terms/${type}/versions/${versionId}`),

  getPublicCompanySettings: () =>
    publicApiClient<Omit<BusinessSettings, 'id' | 'updatedBy' | 'createdAt' | 'updatedAt'>>('/settings/company/public'),

  getPublicLatestTerms: (type: TermsType) =>
    publicApiClient<Omit<TermsVersion, 'id' | 'active' | 'createdBy' | 'createdAt'>>(`/settings/terms/${type}/latest`),
};
