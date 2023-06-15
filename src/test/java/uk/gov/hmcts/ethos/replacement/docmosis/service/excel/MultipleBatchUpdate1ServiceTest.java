package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.util.ArrayList;
import java.util.TreeMap;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(SpringExtension.class)
public class MultipleBatchUpdate1ServiceTest {

    @Mock
    private MultipleHelperService multipleHelperService;

    @InjectMocks
    private MultipleBatchUpdate1Service multipleBatchUpdate1Service;

    private TreeMap<String, Object> multipleObjectsFlags;
    private MultipleDetails multipleDetails;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleObjectsFlags = MultipleUtil.getMultipleObjectsFlags();
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        userToken = "authString";
    }

    @Test
    public void batchUpdate1Logic() {

        multipleBatchUpdate1Service.batchUpdate1Logic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjectsFlags);

        verify(multipleHelperService, times(1))
                .sendUpdatesToSinglesWithConfirmation(userToken, multipleDetails, new ArrayList<>(),
                        multipleObjectsFlags, null);
        verifyNoMoreInteractions(multipleHelperService);

    }

}