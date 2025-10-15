import { useState } from 'react';
import Layout from '../components/layout/Layout';
import { useUserStore } from '../store/userStore';

function UserProfile() {
  const { user } = useUserStore();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    name: user?.name || '',
    email: user?.email || '',
    phone: user?.phone || ''
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
      setIsEditing(false);
  };

  if (!user) {
    return (
      <Layout>
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
          <div className="text-center">
            <p className="text-gray-600">로그인이 필요합니다.</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
            <div className="flex items-center gap-4 mb-8">
              <div className="w-16 h-16 bg-hana-green/10 rounded-full flex items-center justify-center">
                <svg className="w-8 h-8 text-hana-green" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <div>
                <h1 className="text-2xl font-hana-medium text-gray-900">내 정보</h1>
                <p className="text-gray-600 font-hana-light">개인 정보를 확인하고 수정할 수 있습니다.</p>
              </div>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    이름
                  </label>
                  {isEditing ? (
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                      required
                    />
                  ) : (
                    <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                      {user.name}
                    </div>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    이메일
                  </label>
                  {isEditing ? (
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                      required
                    />
                  ) : (
                    <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                      {user.email}
                    </div>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    전화번호
                  </label>
                  {isEditing ? (
                    <input
                      type="tel"
                      name="phone"
                      value={formData.phone}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                      required
                    />
                  ) : (
                    <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                      {user.phone}
                    </div>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    CI
                  </label>
                  <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900 font-mono text-sm">
                    {user.ci}
                  </div>
                  </div>
                </div>

              <div className="flex justify-end gap-4 pt-6 border-t border-gray-200">
                {isEditing ? (
                  <>
                    <button
                      type="button"
                      onClick={() => setIsEditing(false)}
                      className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-hana-medium"
                    >
                      취소
                    </button>
                    <button
                      type="submit"
                      className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium"
                    >
                      저장
                    </button>
                  </>
                ) : (
                <button
                    type="button"
                    onClick={() => setIsEditing(true)}
                    className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium"
                  >
                    수정
                </button>
                )}
              </div>
            </form>
            </div>
          </div>
      </div>
    </Layout>
  );
}

export default UserProfile;