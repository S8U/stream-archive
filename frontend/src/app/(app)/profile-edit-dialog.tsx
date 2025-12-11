'use client';

import { useState, useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useGetUserMe, useUpdateUserMe, useUpdateUserPassword } from "@/lib/api/endpoints/user/user";

interface ProfileEditDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function ProfileEditDialog({ open, onOpenChange }: ProfileEditDialogProps) {
  const queryClient = useQueryClient();
  const { data: user } = useGetUserMe();
  
  // 프로필 수정 폼
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  
  // 비밀번호 변경 폼
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const updateMeMutation = useUpdateUserMe();
  const changePasswordMutation = useUpdateUserPassword();

  // Dialog 열릴 때 유저 정보로 초기값 설정
  useEffect(() => {
    if (open && user) {
      setName(user.name);
      setEmail(user.email);
    }
    if (!open) {
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    }
  }, [open, user]);

  const handleUpdateProfile = async () => {
    try {
      await updateMeMutation.mutateAsync({
        data: { name, email }
      });
      queryClient.invalidateQueries({ queryKey: ['/users/me'] });
      toast.success('프로필이 수정되었습니다.');
      onOpenChange(false);
    } catch {
      toast.error('프로필 수정에 실패했습니다.');
    }
  };

  const handleChangePassword = async () => {
    if (newPassword !== confirmPassword) {
      toast.error('새 비밀번호가 일치하지 않습니다.');
      return;
    }
    if (newPassword.length < 4) {
      toast.error('비밀번호는 4자 이상이어야 합니다.');
      return;
    }

    try {
      await changePasswordMutation.mutateAsync({
        data: { currentPassword, newPassword }
      });
      toast.success('비밀번호가 변경되었습니다.');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      onOpenChange(false);
    } catch {
      toast.error('비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인해주세요.');
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>내 정보</DialogTitle>
          <DialogDescription>
            프로필 정보를 수정하거나 비밀번호를 변경할 수 있습니다.
          </DialogDescription>
        </DialogHeader>

        <Tabs defaultValue="profile" className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="profile">프로필</TabsTrigger>
            <TabsTrigger value="password">비밀번호</TabsTrigger>
          </TabsList>

          <TabsContent value="profile" className="space-y-4 mt-4">
            <div className="space-y-2">
              <Label htmlFor="name">이름</Label>
              <Input
                id="name"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="email">이메일</Label>
              <Input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <Button
              onClick={handleUpdateProfile}
              disabled={updateMeMutation.isPending}
              className="w-full"
            >
              {updateMeMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              저장
            </Button>
          </TabsContent>

          <TabsContent value="password" className="space-y-4 mt-4">
            <div className="space-y-2">
              <Label htmlFor="currentPassword">현재 비밀번호</Label>
              <Input
                id="currentPassword"
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="newPassword">새 비밀번호</Label>
              <Input
                id="newPassword"
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">새 비밀번호 확인</Label>
              <Input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
            </div>
            <Button
              onClick={handleChangePassword}
              disabled={changePasswordMutation.isPending}
              className="w-full"
            >
              {changePasswordMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              비밀번호 변경
            </Button>
          </TabsContent>
        </Tabs>
      </DialogContent>
    </Dialog>
  );
}
