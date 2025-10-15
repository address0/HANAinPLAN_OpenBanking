type InputFieldProps = {
  label: string
  value: string | number
  onChange: (value: string) => void
  type?: 'text' | 'number' | 'email'
  placeholder?: string
  unit?: string
  required?: boolean
  className?: string
}

function InputField({
  label,
  value,
  onChange,
  type = 'text',
  placeholder,
  unit,
  required = false,
  className = ''
}: InputFieldProps) {
  return (
    <div className={`flex flex-col gap-2 ${className}`}>
      <label className="text-sm font-hana-medium text-gray-700">
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <div className="relative">
        <input
          type={type}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          required={required}
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-hana-green transition-colors font-hana-regular"
        />
        {unit && (
          <span className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 font-hana-medium text-sm">
            {unit}
          </span>
        )}
      </div>
    </div>
  )
}

export default InputField