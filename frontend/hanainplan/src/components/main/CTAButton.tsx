
type CTAButtonProps = {
  label: string
  href?: string
  onClick?: () => void
  className?: string
}

function CTAButton({ label, href, onClick, className = '' }: CTAButtonProps) {
  const baseClass =
    'inline-flex items-center justify-center rounded-lg border border-hana-green text-hana-green shadow-[0_0_4px_rgba(0,132,133,0.3)] hover:shadow-md hover:bg-hana-green/5 active:bg-hana-green/10 transition-colors px-6 py-3 w-full sm:w-auto text-base sm:text-lg font-hana-medium'

  if (href) {
    return (
      <a href={href} className={`${baseClass} ${className}`}>
        {label}
      </a>
    )
  }

  return (
    <button type="button" onClick={onClick} className={`${baseClass} ${className}`}>
      {label}
    </button>
  )
}

export default CTAButton