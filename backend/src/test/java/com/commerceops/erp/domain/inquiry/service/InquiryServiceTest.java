package com.commerceops.erp.domain.inquiry.service;

import com.commerceops.erp.domain.inquiry.dto.InquiryAnswerRequest;
import com.commerceops.erp.domain.inquiry.entity.Inquiry;
import com.commerceops.erp.domain.inquiry.enums.InquiryStatus;
import com.commerceops.erp.domain.inquiry.enums.InquiryType;
import com.commerceops.erp.domain.inquiry.repository.InquiryRepository;
import com.commerceops.erp.domain.notification.service.NotificationService;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.user.enums.UserRole;
import com.commerceops.erp.domain.user.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock private InquiryRepository inquiryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private InquiryService inquiryService;

    @Test
    void answerInquiryChangesStatusAndStoresAnswer() {
        User user = User.builder()
                .id(1L).email("user@example.com").password("encoded").name("테스트 사용자")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();
        Inquiry inquiry = Inquiry.builder()
                .id(2L).user(user).type(InquiryType.PRODUCT).subject("재입고 문의")
                .content("언제 재입고되나요?").status(InquiryStatus.WAITING).build();
        when(inquiryRepository.findById(2L)).thenReturn(Optional.of(inquiry));

        inquiryService.answerInquiry(2L, new InquiryAnswerRequest("다음 주 입고 예정입니다."));

        assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.ANSWERED);
        assertThat(inquiry.getAnswer()).isEqualTo("다음 주 입고 예정입니다.");
    }
}
