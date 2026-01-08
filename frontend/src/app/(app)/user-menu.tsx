'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { LogOut, Settings, User } from 'lucide-react';
import { useQueryClient } from '@tanstack/react-query';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { useGetUserMe } from '@/lib/api/endpoints/user/user';
import { useLogout } from '@/lib/api/endpoints/auth/auth';
import { ProfileEditDialog } from './profile-edit-dialog';

export function UserMenu() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { data: user, isLoading, isError } = useGetUserMe();
  const logoutMutation = useLogout();
  const [profileDialogOpen, setProfileDialogOpen] = useState(false);

  const handleLogout = async () => {
    try {
      await logoutMutation.mutateAsync({ data: {} });
      queryClient.clear();
      router.push('/');
    } catch {
      queryClient.clear();
      router.push('/');
    }
  };

  // 로딩 중이거나 에러(401)면 로그인 버튼 표시
  if (isLoading || isError || !user) {
    return (
      <Button variant="secondary" asChild>
        <Link href="/login">로그인</Link>
      </Button>
    );
  }

  const isAdmin = user.role === 'ADMIN';

  return (
    <>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="icon" className="rounded-full">
            <Avatar className="w-8 h-8">
              <AvatarFallback>{user.name[0]}</AvatarFallback>
            </Avatar>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="w-48">
          <div className="px-2 py-1.5">
            <p className="text-sm font-medium">{user.name}</p>
          </div>
          <DropdownMenuSeparator />

          <DropdownMenuItem onClick={() => setProfileDialogOpen(true)} className="flex items-center gap-2">
            <User className="w-4 h-4" />
            내 정보
          </DropdownMenuItem>

          {isAdmin && (
            <>
              <DropdownMenuItem asChild>
                <Link href="/admin" target="_blank" className="flex items-center gap-2">
                  <Settings className="w-4 h-4" />
                  관리 페이지
                </Link>
              </DropdownMenuItem>
            </>
          )}

          <DropdownMenuSeparator />
          <DropdownMenuItem
            onClick={handleLogout}
            className="flex items-center gap-2 text-destructive focus:text-destructive"
          >
            <LogOut className="w-4 h-4" />
            로그아웃
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      <ProfileEditDialog open={profileDialogOpen} onOpenChange={setProfileDialogOpen} />
    </>
  );
}
