"use client";

import { TableHead } from "@/components/ui/table";
import { cn } from "@/lib/utils";
import { ChevronDown, ChevronUp, ChevronsUpDown } from "lucide-react";
import type { ReactNode } from "react";

type SortDirection = "asc" | "desc";

type SortableTableHeadProps<T extends string> = {
    field: T;
    currentField: T;
    currentDirection: SortDirection;
    onSort: (field: T) => void;
    children: ReactNode;
    className?: string;
    align?: "left" | "center";
};

export function SortableTableHead<T extends string>({
    field,
    currentField,
    currentDirection,
    onSort,
    children,
    className,
    align = "left",
}: SortableTableHeadProps<T>) {
    const isActive = currentField === field;
    const Icon = isActive ? (currentDirection === "asc" ? ChevronUp : ChevronDown) : ChevronsUpDown;

    return (
        <TableHead className={className}>
            <button
                type="button"
                className={cn(
                    "flex w-full cursor-pointer items-center gap-1 font-semibold hover:text-foreground",
                    align === "center" && "justify-center"
                )}
                onClick={() => onSort(field)}
            >
                {/* center 정렬 시 아이콘 폭만큼 좌측에 보이지 않는 스페이서를 둬 텍스트를 실제 중앙에 맞춘다 */}
                {align === "center" && <span aria-hidden className="h-3.5 w-3.5 shrink-0" />}
                <span>{children}</span>
                <Icon className={cn("h-3.5 w-3.5 shrink-0", !isActive && "text-muted-foreground opacity-40")} />
            </button>
        </TableHead>
    );
}
