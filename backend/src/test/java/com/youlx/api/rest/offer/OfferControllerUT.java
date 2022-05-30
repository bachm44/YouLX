package com.youlx.api.rest.offer;

import com.youlx.api.Routes;
import com.youlx.domain.offer.*;
import com.youlx.domain.user.User;
import com.youlx.domain.user.UserId;
import com.youlx.domain.utils.exception.ApiCustomException;
import com.youlx.domain.utils.exception.ApiNotFoundException;
import com.youlx.testUtils.MvcHelpers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;

import static com.youlx.testUtils.Fixtures.offer;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OfferControllerUT {
    @Autowired
    private MvcHelpers helpers;

    @MockBean
    private OfferModifyService service;
    @MockBean
    private OfferFindService offerFindService;
    @MockBean
    private OfferStateCheckService offerStateCheckService;

    private static final String url = Routes.Offer.OFFERS;

    @Nested
    class CloseTests {
        @Test
        void unauthorized() throws Exception {
            helpers.postRequest(null, url + "/123/close").andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser("user")
        void close() throws Exception {
            final var id = "a";
            final var user = new User(List.of(), "", "", "", "", "user", "");
            final var offer = new Offer(null, null, user, null);
            when(service.close(id, new OfferClose(OfferCloseReason.MANUAL), new UserId("user"))).thenReturn(offer);

            helpers.postRequest(null, url + "/" + id + "/close").andExpect(status().isOk());
        }

        @Test
        @WithMockUser("user")
        void notFound() throws Exception {
            final var id = "a";
            doThrow(new ApiNotFoundException("")).when(service).close(id, new OfferClose(OfferCloseReason.MANUAL), new UserId("user"));

            helpers.postRequest(null, url + "/" + id + "/close").andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser("user")
        void otherError() throws Exception {
            final var id = "a";
            doThrow(new ApiCustomException("")).when(service).close(id, new OfferClose(OfferCloseReason.MANUAL), new UserId("user"));

            helpers.postRequest(null, url + "/" + id + "/close").andExpect(status().isBadRequest());
        }
    }

    @Nested
    class SearchTests {
        @Test
        void search() throws Exception {
            final var query = "asdf";
            helpers.getRequest(Routes.Offer.OFFERS + "/search?query=" + query).andExpect(status().isOk());

            verify(offerFindService).search(new UserId(), query);
        }
    }

    @Nested
    class GetTests {
        @Test
        void get() throws Exception {
            final var id = "a";
            when(offerFindService.findById(id)).thenReturn(Optional.of(offer));
            when(offerStateCheckService.isVisible(new UserId(), id)).thenReturn(true);

            helpers.getRequest(Routes.Offer.OFFERS + "/" + id).andExpect(status().isOk());
        }

        @Test
        @WithMockUser("user")
        void getAuthenticated() throws Exception {
            final var id = "a";
            when(offerFindService.findById(id)).thenReturn(Optional.of(offer));
            when(offerStateCheckService.isVisible(new UserId("user"), id)).thenReturn(true);
            helpers.getRequest(Routes.Offer.OFFERS + "/" + id).andExpect(status().isOk());
        }

        @Test
        void forbidden() throws Exception {
            final var id = "a";
            when(offerStateCheckService.isVisible(new UserId(), id)).thenReturn(false);
            helpers.getRequest(Routes.Offer.OFFERS + "/" + id).andExpect(status().isForbidden());
        }

        @Test
        void notFound() throws Exception {
            final var id = "a";
            when(offerFindService.findById(id)).thenReturn(Optional.empty());
            when(offerStateCheckService.isVisible(new UserId(), id)).thenReturn(true);
            helpers.getRequest(Routes.Offer.OFFERS + "/" + id).andExpect(status().isNotFound());
        }
    }
}
