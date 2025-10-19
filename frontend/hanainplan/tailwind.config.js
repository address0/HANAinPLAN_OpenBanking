/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        'hana-light': ['Hana2-Light', 'sans-serif'],
        'hana-regular': ['Hana2-Regular', 'sans-serif'],
        'hana-medium': ['Hana2-Medium', 'sans-serif'],
        'hana-bold': ['Hana2-Bold', 'sans-serif'],
        'hana-heavy': ['Hana2-Heavy', 'sans-serif'],
        'hana-cm': ['Hana2-CM', 'sans-serif'],
      },
      colors: {
        'hana-green': '#008485',
        'hana-light-green': '#E8F5F4',
      },
      animation: {
        'bounce-gentle': 'bounce-gentle 2s ease-in-out infinite',
        'fade-in': 'fade-in 0.5s ease-in-out',
      },
      keyframes: {
        'bounce-gentle': {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-10px)' },
        },
        'fade-in': {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0px)' },
        },
      },
    },
  },
  plugins: [
    require('@tailwindcss/typography'),
  ],
}

