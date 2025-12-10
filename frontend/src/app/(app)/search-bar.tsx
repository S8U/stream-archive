'use client';

import { useState, FormEvent } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Search } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

export function SearchBar() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [query, setQuery] = useState(searchParams.get('q') || '');

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    const trimmed = query.trim();
    if (trimmed) {
      router.push(`/?q=${encodeURIComponent(trimmed)}`);
    } else {
      router.push('/');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex-1 max-w-sm">
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
