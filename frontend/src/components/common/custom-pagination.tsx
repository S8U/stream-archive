"use client";

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
    page: number;
    totalPages: number;
    onPageChange?: (page: number) => void;
}

const getPageNumbers = (currentPage: number, totalPages: number) => {
    const pages: (number | 'ellipsis')[] = [];

    for (let i = 0; i < totalPages; i++) {
        // 첫 페이지, 마지막 페이지, 현재 페이지 주변 1개씩 표시
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

export function CustomPagination({ page, totalPages, onPageChange }: CustomPaginationProps) {
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();

    if (totalPages <= 1) return null;

    const pageNumbers = getPageNumbers(page, totalPages);
    const isFirstPage = page === 0;
    const isLastPage = page >= totalPages - 1;

    const handlePageChange = (newPage: number) => {
        // 콜백 호출 (있는 경우)
        onPageChange?.(newPage);

        // URL 업데이트
        const params = new URLSearchParams(searchParams.toString());
        if (newPage === 0) {
            params.delete("page");
        } else {
            params.set("page", String(newPage + 1));
        }
        const queryString = params.toString();
        router.push(queryString ? `${pathname}?${queryString}` : pathname);
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
