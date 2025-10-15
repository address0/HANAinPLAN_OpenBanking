type ProductCardProps = {
  imageSrc: string
  title: string
  description: string
  alt: string
}

function ProductCard({ imageSrc, title, description, alt }: ProductCardProps) {
  return (
    <div className="bg-white rounded-xl shadow-md hover:shadow-lg transition-shadow overflow-hidden w-full max-w-[320px] group">
      <div className="h-44 sm:h-48 w-full overflow-hidden">
        <img src={imageSrc} alt={alt} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300 ease-out" />
      </div>
      <div className="p-4">
        <h3 className="text-xl font-hana-medium text-gray-900">{title}</h3>
        <p className="mt-2 text-gray-600 font-hana-medium text-sm sm:text-base leading-5">
          {description}
        </p>
      </div>
    </div>
  )
}

export default ProductCard