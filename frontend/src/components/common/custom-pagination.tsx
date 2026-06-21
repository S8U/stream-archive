"use client";

import { Suspense } from "react";
import { useQueryState, parseAsInteger } from "nuqs";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import {
    Pagination,
    PaginationContent,
    PaginationEllipsis,
    PaginationItem,
    PaginationLink,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination";

interface CustomPaginationProps {
    totalPages: number;
    /** 외부에서 page 상태를 직접 관리할 경우 전달 (0-based) */
    page?: number;
    /** 외부에서 page 변경 핸들러를 전달할 경우 (0-based) */
    onPageChange?: (page: number) => void;
    /** true면 nuqs 사용 (CSR), false면 router.push 사용 (SSR 페이지용) */
    useShallowRouting?: boolean;
    /** 현재 페이지 양옆에 보여줄 페이지 개수 (기본 1) */
    siblingCount?: number;
}

/**
 * 표시할 페이지 번호 목록을 만든다 (모두 0-based).
 *
 * 항상 첫 페이지·마지막 페이지·현재 페이지 주변(sibling)을 포함하며,
 * 그 사이에 생략된 구간이 있으면 'ellipsis'를 끼워 넣는다.
 * 생략으로 한 페이지만 가려질 바엔 그 페이지를 그대로 보여준다(점 하나 = 칸 하나).
 */
const getPageNumbers = (
    currentPage: number,
    totalPages: number,
    siblingCount: number,
): (number | "ellipsis")[] => {
    // 첫/끝 페이지 + 현재 주변(sibling*2+1) + ellipsis 자리 2개를 모두 합쳐도
    // 전체 페이지 수보다 많거나 같으면 그냥 전부 나열한다.
    const totalSlots = siblingCount * 2 + 5;
    if (totalPages <= totalSlots) {
        return Array.from({ length: totalPages }, (_, i) => i);
    }

    const firstPage = 0;
    const lastPage = totalPages - 1;
    const leftSibling = Math.max(currentPage - siblingCount, firstPage);
    const rightSibling = Math.min(currentPage + siblingCount, lastPage);

    // ellipsis가 2칸 이상을 가릴 때만 쓴다.
    // 1칸만 가려질 거면 점(…) 대신 그 페이지 번호를 그대로 보여주는 게 깔끔하다.
    const showLeftEllipsis = leftSibling > firstPage + 2;
    const showRightEllipsis = rightSibling < lastPage - 2;

    const pages: (number | "ellipsis")[] = [firstPage];

    if (showLeftEllipsis) {
        pages.push("ellipsis");
    } else {
        // ellipsis가 없으면 첫 페이지와 leftSibling 사이를 채운다.
        for (let i = firstPage + 1; i < leftSibling; i++) pages.push(i);
    }

    for (let i = leftSibling; i <= rightSibling; i++) {
        if (i !== firstPage && i !== lastPage) pages.push(i);
    }

    if (showRightEllipsis) {
        pages.push("ellipsis");
    } else {
        for (let i = rightSibling + 1; i < lastPage; i++) pages.push(i);
    }

    pages.push(lastPage);

    return pages;
};

export function CustomPagination(props: CustomPaginationProps) {
    return (
        <Suspense fallback={null}>
            <CustomPaginationInner {...props} />
        </Suspense>
    );
}

function CustomPaginationInner({
    totalPages,
    page: externalPage,
    onPageChange,
    useShallowRouting = false,
    siblingCount = 1,
}: CustomPaginationProps) {
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();

    // nuqs로 URL 상태 관리 (1-based) - 외부 상태가 없을 때만 사용
    const [urlPage, setUrlPage] = useQueryState("page", parseAsInteger.withDefault(1));

    // 외부 page가 전달되면 사용, 아니면 URL 상태 사용 (0-based로 정규화)
    const rawPage = externalPage ?? urlPage - 1;
    // 범위를 벗어난 값이 들어와도 깨지지 않도록 클램프한다.
    const page = Math.min(Math.max(rawPage, 0), Math.max(totalPages - 1, 0));

    if (totalPages <= 1) return null;

    const pageNumbers = getPageNumbers(page, totalPages, siblingCount);
    const isFirstPage = page <= 0;
    const isLastPage = page >= totalPages - 1;

    const goToPage = (newPage: number) => {
        const target = Math.min(Math.max(newPage, 0), totalPages - 1);
        if (target === page) return;

        if (onPageChange) {
            // 외부 핸들러가 있으면 호출 (URL 업데이트는 외부에서)
            onPageChange(target);
        } else if (useShallowRouting) {
            // shallow routing 모드 (CSR 페이지용)
            setUrlPage(target + 1);
        } else {
            // 전체 탐색 모드 (SSR 페이지용)
            const params = new URLSearchParams(searchParams.toString());
            params.set("page", String(target + 1));
            router.push(`${pathname}?${params.toString()}`);
        }
    };

    return (
        <Pagination className="mt-4">
            <PaginationContent>
                <PaginationItem>
                    <PaginationPrevious
                        href="#"
                        onClick={(e) => {
                            e.preventDefault();
                            goToPage(page - 1);
                        }}
                        aria-disabled={isFirstPage}
                        tabIndex={isFirstPage ? -1 : undefined}
                        className={isFirstPage ? "pointer-events-none opacity-50" : ""}
                    />
                </PaginationItem>

                {pageNumbers.map((pageNum, idx) =>
                    pageNum === "ellipsis" ? (
                        <PaginationItem key={`ellipsis-${idx}`}>
                            <PaginationEllipsis />
                        </PaginationItem>
                    ) : (
                        <PaginationItem key={pageNum}>
                            <PaginationLink
                                href="#"
                                isActive={page === pageNum}
                                onClick={(e) => {
                                    e.preventDefault();
                                    goToPage(pageNum);
                                }}
                            >
                                {pageNum + 1}
                            </PaginationLink>
                        </PaginationItem>
                    ),
                )}

                <PaginationItem>
                    <PaginationNext
                        href="#"
                        onClick={(e) => {
                            e.preventDefault();
                            goToPage(page + 1);
                        }}
                        aria-disabled={isLastPage}
                        tabIndex={isLastPage ? -1 : undefined}
                        className={isLastPage ? "pointer-events-none opacity-50" : ""}
                    />
                </PaginationItem>
            </PaginationContent>
        </Pagination>
    );
}
