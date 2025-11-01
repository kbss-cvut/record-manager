package cz.cvut.kbss.study.rest.handler;

import cz.cvut.kbss.study.dto.RecordDto;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.rest.event.PaginatedResultRetrievedEvent;
import cz.cvut.kbss.study.rest.util.HttpPaginationLink;
import cz.cvut.kbss.study.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HateoasPagingListenerTest {

    private static final String BASE_URL = "http://localhost/rest/records";

    private UriComponentsBuilder uriBuilder;
    private MockHttpServletResponse responseMock;

    private List<RecordDto> records;

    private HateoasPagingListener listener;

    @BeforeEach
    public void setUp() {
        this.listener = new HateoasPagingListener();
        this.uriBuilder = UriComponentsBuilder.newInstance().scheme("http").host("localhost").path("rest/records");
        this.responseMock = new MockHttpServletResponse();
        final User author = Generator.generateUser(null, null);
        this.records = IntStream.range(0, 10).mapToObj(i -> Generator.generateRecordDto(author))
                                .collect(Collectors.toList());
    }

    @Test
    public void generatesNextRelativeLink() {
        final int size = 5;
        final Page<RecordDto> page =
                new PageImpl<>(records.subList(0, size), PageRequest.of(0, size), records.size());
        listener.onApplicationEvent(event(page));
        final String linkHeader = responseMock.getHeader(HttpHeaders.LINK);
        assertNotNull(linkHeader);
        final String nextLink = HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.NEXT.getName());
        assertThat(nextLink, containsString(BASE_URL));
        assertThat(nextLink, containsString(page(1)));
        assertThat(nextLink, containsString(pageSize(size)));
    }

    private static String page(int pageNo) {
        return Constants.PAGE_PARAM + "=" + pageNo;
    }

    private static String pageSize(int size) {
        return Constants.PAGE_SIZE_PARAM + "=" + size;
    }

    private PaginatedResultRetrievedEvent event(Page<RecordDto> page) {
        return new PaginatedResultRetrievedEvent(this, uriBuilder, responseMock, page);
    }

    @Test
    public void generatesLastRelativeLink() {
        final int size = 5;
        final Page<RecordDto> page =
                new PageImpl<>(records.subList(0, size), PageRequest.of(0, size), records.size());
        listener.onApplicationEvent(event(page));
        final String linkHeader = responseMock.getHeader(HttpHeaders.LINK);
        assertNotNull(linkHeader);
        final String lastLink = HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.LAST.getName());
        assertThat(lastLink, containsString(BASE_URL));
        assertThat(lastLink, containsString(page(1)));
        assertThat(lastLink, containsString(pageSize(size)));
    }

    @Test
    public void generatesPreviousRelativeLink() {
        final int size = 5;
        final Page<RecordDto> page =
                new PageImpl<>(records.subList(size, records.size()), PageRequest.of(1, size),
                               records.size());
        listener.onApplicationEvent(event(page));
        final String linkHeader = responseMock.getHeader(HttpHeaders.LINK);
        assertNotNull(linkHeader);
        final String lastLink = HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.PREVIOUS.getName());
        assertThat(lastLink, containsString(BASE_URL));
        assertThat(lastLink, containsString(page(0)));
        assertThat(lastLink, containsString(pageSize(size)));
    }

    @Test
    public void generatesFirstRelativeLink() {
        final int size = 5;
        final Page<RecordDto> page =
                new PageImpl<>(records.subList(size, records.size()), PageRequest.of(1, size),
                               records.size());
        listener.onApplicationEvent(event(page));
        final String linkHeader = responseMock.getHeader(HttpHeaders.LINK);
        assertNotNull(linkHeader);
        final String lastLink = HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.FIRST.getName());
        assertThat(lastLink, containsString(BASE_URL));
        assertThat(lastLink, containsString(page(0)));
        assertThat(lastLink, containsString(pageSize(size)));
    }

    @Test
    public void generatesAllRelativeLinks() {
        final int size = 3;
        final int pageNum = 2;
        final Page<RecordDto> page = new PageImpl<>(records.subList(pageNum * size, pageNum * size + size),
                                                           PageRequest.of(pageNum, size), records.size());
        listener.onApplicationEvent(event(page));
        final String linkHeader = responseMock.getHeader(HttpHeaders.LINK);
        assertNotNull(linkHeader);
        final String nextLink = HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.NEXT.getName());
        assertThat(nextLink, containsString(page(pageNum + 1)));
        assertThat(nextLink, containsString(pageSize(size)));
        final String previousLink =
                HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.PREVIOUS.getName());
        assertThat(previousLink, containsString(page(pageNum - 1)));
        assertThat(previousLink, containsString(pageSize(size)));
        final String firstLink = HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.FIRST.getName());
        assertThat(firstLink, containsString(page(0)));
        assertThat(firstLink, containsString(pageSize(size)));
        final String lastLink = HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.LAST.getName());
        assertThat(lastLink, containsString(page(3)));
        assertThat(lastLink, containsString(pageSize(size)));
    }

    @Test
    public void generatesNoLinksForEmptyPage() {
        final int size = 5;
        final Page<RecordDto> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, size), 0);
        listener.onApplicationEvent(event(page));
        final String linkHeader = responseMock.getHeader(HttpHeaders.LINK);
        assertNull(linkHeader);
    }

    @Test
    public void generatesPreviousAndFirstLinkForEmptyPageAfterEnd() {
        final int size = 5;
        final int pageNum = 4;
        final Page<RecordDto> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(pageNum, size),
                                                           records.size());
        listener.onApplicationEvent(event(page));
        final String linkHeader = responseMock.getHeader(HttpHeaders.LINK);
        assertNotNull(linkHeader);
        final String previousLink =
                HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.PREVIOUS.getName());
        assertThat(previousLink, containsString(page(pageNum - 1)));
        assertThat(previousLink, containsString(pageSize(size)));
        final String firstLink = HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.FIRST.getName());
        assertThat(firstLink, containsString(page(0)));
        assertThat(firstLink, containsString(pageSize(size)));
    }

    @Test
    public void generatesFirstAndLastLinksForOnlyPage() {
        final int size = records.size();
        final Page<RecordDto> page = new PageImpl<>(records, PageRequest.of(0, size), records.size());
        listener.onApplicationEvent(event(page));
        final String linkHeader = responseMock.getHeader(HttpHeaders.LINK);
        assertNotNull(linkHeader);
        final String firstLink = HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.FIRST.getName());
        assertThat(firstLink, containsString(page(0)));
        assertThat(firstLink, containsString(pageSize(size)));
        final String lastLink = HttpLinkHeaderUtil.extractURIByRel(linkHeader, HttpPaginationLink.LAST.getName());
        assertThat(lastLink, containsString(page(0)));
        assertThat(lastLink, containsString(pageSize(size)));
    }

    @Test
    public void generatesTotalCountHeader() {
        final int size = records.size();
        final Page<RecordDto> page = new PageImpl<>(records, PageRequest.of(0, size / 2), records.size());
        listener.onApplicationEvent(event(page));
        final String totalCountHeader = responseMock.getHeader(Constants.X_TOTAL_COUNT_HEADER);
        assertNotNull(totalCountHeader);
        assertEquals(size, Integer.parseInt(totalCountHeader));
    }
}