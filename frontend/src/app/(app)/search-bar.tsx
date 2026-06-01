'use client';

import { useState, useRef, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Search, ArrowLeft } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

function useSearch() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [query, setQuery] = useState(searchParams.get('q') || '');

  const submit = () => {
    const trimmed = query.trim();
    router.push(trimmed ? `/?q=${encodeURIComponent(trimmed)}` : '/');
  };

  return { query, setQuery, submit };
}

/** PC: 헤더 중앙에 항상 펼쳐진 인라인 검색창 */
export function SearchBar() {
  const { query, setQuery, submit } = useSearch();

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        submit();
      }}
      className="hidden md:block flex-1 max-w-sm"
    >
      <div className="flex border border-input rounded-full h-10 focus-within:ring-2 focus-within:ring-ring transition-all">
        <Input
          type="search"
          placeholder="검색"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="flex-1 border-0 h-full focus-visible:ring-0"
        />
        <Button type="submit" variant="secondary" size="icon" className="rounded-r-full h-full">
          <Search className="h-4 w-4" />
        </Button>
      </div>
    </form>
  );
}

/** 모바일: 검색 아이콘 버튼 + 탭하면 펼쳐지는 전체폭 오버레이 검색바 */
export function MobileSearchButton() {
  const { query, setQuery, submit } = useSearch();
  const [open, setOpen] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (open) {
      inputRef.current?.focus();
    }
  }, [open]);

  const handleSubmit = () => {
    submit();
    setOpen(false);
  };

  return (
    <>
      <Button
        variant="ghost"
        size="icon"
        className="md:hidden"
        onClick={() => setOpen(true)}
        aria-label="검색 열기"
      >
        <Search className="h-5 w-5" />
      </Button>

      {open && (
        <div className="md:hidden fixed inset-x-0 top-0 h-14 bg-background z-50 flex items-center gap-2 px-2 animate-in fade-in slide-in-from-top-1 duration-150">
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="flex-shrink-0"
            onClick={() => setOpen(false)}
            aria-label="검색 닫기"
          >
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSubmit();
            }}
            className="flex-1"
          >
            <div className="flex border border-input rounded-full h-10 focus-within:ring-2 focus-within:ring-ring transition-all">
              <Input
                ref={inputRef}
                type="search"
                placeholder="검색"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                className="flex-1 border-0 h-full focus-visible:ring-0"
              />
              <Button type="submit" variant="secondary" size="icon" className="rounded-r-full h-full">
                <Search className="h-4 w-4" />
              </Button>
            </div>
          </form>
        </div>
      )}
    </>
  );
}
