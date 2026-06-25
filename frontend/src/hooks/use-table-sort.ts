import { useQueryState, parseAsStringLiteral } from "nuqs";

const sortDirectionOptions = ["asc", "desc"] as const;

type SortDirection = typeof sortDirectionOptions[number];

type UseTableSortResult<T extends string> = {
    sortField: T;
    sortDirection: SortDirection;
    sortParam: string;
    handleSort: (field: T) => void;
    resetSort: () => void;
};

/**
 * 관리자 목록 테이블의 컬럼 정렬 상태를 URL(nuqs)에 묶어 관리한다.
 *
 * 같은 컬럼을 누를 때마다 오름차순 → 내림차순 → 기본(id) 순으로 토글한다.
 * 정렬이 바뀌면 1페이지로 되돌린다.
 */
export function useTableSort<T extends string>(
    sortFieldOptions: readonly T[],
    setPage: (page: number) => void
): UseTableSortResult<T> {
    const defaultField = sortFieldOptions[0]

    const [sortField, setSortField] = useQueryState(
        "sort",
        parseAsStringLiteral(sortFieldOptions).withDefault(defaultField)
    );
    const [sortDirection, setSortDirection] = useQueryState(
        "dir",
        parseAsStringLiteral(sortDirectionOptions).withDefault("desc")
    );

    // 다른 컬럼이면 오름차순부터, 같은 컬럼이면 내림차순 → 기본(id 내림차순)으로 토글
    const handleSort = (field: T) => {
        if (sortField !== field) {
            setSortField(field);
            setSortDirection("asc");
            setPage(1);
            return;
        }

        if (sortDirection === "asc") {
            setSortDirection("desc");
            setPage(1);
            return;
        }

        if (field !== defaultField) {
            setSortField(defaultField);
        }
        setSortDirection(field === defaultField ? "asc" : "desc");
        setPage(1);
    };

    const resetSort = () => {
        setSortField(defaultField);
        setSortDirection("desc");
    };

    return {
        sortField,
        sortDirection,
        sortParam: `${sortField},${sortDirection}`,
        handleSort,
        resetSort
    };
}
