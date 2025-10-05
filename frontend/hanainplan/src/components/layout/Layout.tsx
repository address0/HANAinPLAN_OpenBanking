import { type ReactNode } from 'react';
import Nav from '../main/Nav';
import Footer from '../main/Footer';
import FloatingButtons from '../main/FloatingButtons';

interface LayoutProps {
  children: ReactNode;
  showFloatingButtons?: boolean;
}

function Layout({ children, showFloatingButtons = false }: LayoutProps) {
  return (
    <div className="min-h-screen flex flex-col">
      <Nav />
      <main className="flex-1 pt-16 sm:pt-20 lg:pt-24 bg-[#F6F6F6]">
        {children}
      </main>
      <Footer />
      {showFloatingButtons && <FloatingButtons />}
    </div>
  );
}

export default Layout;