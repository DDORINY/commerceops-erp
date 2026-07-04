package com.commerceops.erp.domain.inquiry.service;

import com.commerceops.erp.domain.inquiry.dto.InquiryAnswerRequest;
import com.commerceops.erp.domain.inquiry.dto.InquiryCreateRequest;
import com.commerceops.erp.domain.inquiry.dto.InquiryResponse;
import com.commerceops.erp.domain.inquiry.entity.Inquiry;
import com.commerceops.erp.domain.inquiry.enums.InquiryStatus;
import com.commerceops.erp.domain.inquiry.repository.InquiryRepository;
import com.commerceops.erp.domain.notification.enums.NotificationType;
import com.commerceops.erp.domain.notification.service.NotificationService;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Transactional
    public InquiryResponse createInquiry(Long productId, User user, InquiryCreateRequest request) {
        Product product = null;
        if (productId != null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        }

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .product(product)
                .type(request.type())
                .subject(request.subject())
                .content(request.content())
                .status(InquiryStatus.WAITING)
                .build();

        return InquiryResponse.from(inquiryRepository.save(inquiry));
    }

    public List<InquiryResponse> getMyInquiries(User user) {
        return inquiryRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(InquiryResponse::from).toList();
    }

    public List<InquiryResponse> getProductInquiries(Long productId) {
        return inquiryRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(InquiryResponse::from).toList();
    }

    public PageResponse<InquiryResponse> getAdminInquiries(InquiryStatus status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                inquiryRepository.findAllForAdmin(status, keyword, pageable).map(InquiryResponse::from));
    }

    @Transactional
    public InquiryResponse answerInquiry(Long inquiryId, InquiryAnswerRequest request) {
        Inquiry inquiry = findInquiry(inquiryId);
        if (inquiry.getStatus() == InquiryStatus.ANSWERED) {
            throw new BusinessException(ErrorCode.INQUIRY_ALREADY_ANSWERED);
        }
        inquiry.answer(request.answer());
        notificationService.notifyUser(
                inquiry.getUser(),
                NotificationType.INQUIRY_ANSWERED,
                "Inquiry answered",
                "Your inquiry has been answered: " + inquiry.getSubject(),
                "INQUIRY",
                inquiry.getId()
        );
        return InquiryResponse.from(inquiry);
    }

    @Transactional
    public InquiryResponse closeInquiry(Long inquiryId) {
        Inquiry inquiry = findInquiry(inquiryId);
        inquiry.close();
        return InquiryResponse.from(inquiry);
    }

    private Inquiry findInquiry(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
    }
}
