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
    },
  },
  plugins: [
    require('@tailwindcss/typography'),
  ],
}

