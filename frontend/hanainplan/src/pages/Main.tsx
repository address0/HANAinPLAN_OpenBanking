
import Layout from '../components/layout/Layout'
import Carousel from '../components/main/Carousel'
import MainSection from '../components/main/MainSection'

function Main() {
  return (
    <Layout showFloatingButtons={true}>
      <Carousel />
      <MainSection />
    </Layout>
  )
}

export default Main