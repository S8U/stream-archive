"use client";

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
}

const getPageNumbers = (currentPage: number, totalPages: number) => {
    const pages: (number | 'ellipsis')[] = [];

    for (let i = 0; i < totalPages; i++) {
        const isFirstOrLast = i === 0 || i === totalPages - 1;
        const isNearCurrent = i >= currentPage - 1 && i <= currentPage + 1;

        if (isFirstOrLast || isNearCurrent) {
            pages.push(i);
        } else if (
            (i === currentPage - 2 && currentPage > 2) ||
            (i === currentPage + 2 && currentPage < totalPages - 3)
        ) {
            pages.push('ellipsis');
        }
    }

    return pages;
};

export function CustomPagination({ 
    totalPages, 
    page: externalPage, 
    onPageChange,
    useShallowRouting = false 
}: CustomPaginationProps) {
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();
    
    // nuqs로 URL 상태 관리 (1-based) - shallow routing 모드에서만 사용
    const [urlPage, setUrlPage] = useQueryState("page", parseAsInteger.withDefault(1));

    // 외부 page가 전달되면 사용, 아니면 URL 상태 사용
    const page = externalPage ?? (urlPage - 1);

    if (totalPages <= 1) return null;

    const pageNumbers = getPageNumbers(page, totalPages);
    const isFirstPage = page === 0;
    const isLastPage = page >= totalPages - 1;

    const handlePageChange = (newPage: number) => {
        if (onPageChange) {
            // 외부 핸들러가 있으면 호출 (URL 업데이트는 외부에서)
            onPageChange(newPage);
        } else if (useShallowRouting) {
            // shallow routing 모드 (CSR 페이지용)
            setUrlPage(newPage + 1);
        } else {
            // 전체 탐색 모드 (SSR 페이지용)
            const params = new URLSearchParams(searchParams.toString());
            params.set("page", String(newPage + 1));
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
                            if (!isFirstPage) handlePageChange(page - 1);
                        }}
                        aria-disabled={isFirstPage}
                        className={isFirstPage ? "pointer-events-none opacity-50" : ""}
                    />
                </PaginationItem>

                {pageNumbers.map((pageNum, idx) => {
                    if (pageNum === 'ellipsis') {
                        return (
                            <PaginationItem key={`ellipsis-${idx}`}>
                                <PaginationEllipsis />
                            </PaginationItem>
                        );
                    }

                    return (
                        <PaginationItem key={pageNum}>
                            <PaginationLink
                                href="#"
                                isActive={page === pageNum}
                                onClick={(e) => {
                                    e.preventDefault();
                                    handlePageChange(pageNum);
                                }}
                            >
                                {pageNum + 1}
                            </PaginationLink>
                        </PaginationItem>
                    );
                })}

                <PaginationItem>
                    <PaginationNext
                        href="#"
                        onClick={(e) => {
                            e.preventDefault();
                            if (!isLastPage) handlePageChange(page + 1);
                        }}
                        aria-disabled={isLastPage}
                        className={isLastPage ? "pointer-events-none opacity-50" : ""}
                    />
                </PaginationItem>
            </PaginationContent>
        </Pagination>
    );
}
