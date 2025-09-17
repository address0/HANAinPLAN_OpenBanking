import Layout from '../components/layout/Layout'
import PensionSection from '../components/pension/PensionSection'

function PensionCalculator() {
  return (
    <Layout showFloatingButtons={true}>
      <PensionSection />
    </Layout>
  )
}

export default PensionCalculator