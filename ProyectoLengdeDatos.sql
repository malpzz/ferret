-- Tabla Roles
CREATE TABLE Roles (
    IdRol NUMBER PRIMARY KEY,
    nombre VARCHAR2(100)
);

-- Tabla Usuarios
CREATE TABLE Usuarios (
    IdUsuario NUMBER PRIMARY KEY,
    nombreUsuario VARCHAR2(100),
    contraseña VARCHAR2(100),
    IdRol NUMBER,
    FOREIGN KEY (IdRol) REFERENCES Roles(IdRol)
);

-- Tabla Clientes
CREATE TABLE Clientes (
    IdCliente NUMBER PRIMARY KEY,
    nombreCliente VARCHAR2(100),
    apellidos VARCHAR2(100),
    direccion VARCHAR2(150),
    telefono VARCHAR2(15),
    email VARCHAR2(100)
);

-- Tabla Empleados
CREATE TABLE Empleados (
    IdEmpleado NUMBER PRIMARY KEY,
    nombreEmpleado VARCHAR2(100),
    apellidos VARCHAR2(100),
    direccion VARCHAR2(150),
    telefono VARCHAR2(15),
    puesto VARCHAR2(100)
);

-- Tabla Horarios
CREATE TABLE Horarios (
    IdHorario NUMBER PRIMARY KEY,
    IdEmpleado NUMBER,
    dia DATE,
    hora_entrada NUMBER,
    hora_salida NUMBER,
    FOREIGN KEY (IdEmpleado) REFERENCES Empleados(IdEmpleado)
);

-- Tabla Proveedores
CREATE TABLE Proveedores (
    IdProveedor NUMBER PRIMARY KEY,
    nombreProveedor VARCHAR2(100),
    direccion VARCHAR2(150),
    telefono VARCHAR2(15),
    email VARCHAR2(100)
);

-- Tabla Productos
CREATE TABLE Productos (
    IdProducto NUMBER PRIMARY KEY,
    nombreProducto VARCHAR2(100),
    descripcion VARCHAR2(100),
    precio NUMBER(10,2),
    IdProveedor NUMBER,
    FOREIGN KEY (IdProveedor) REFERENCES Proveedores(IdProveedor)
);

-- Tabla Stock
CREATE TABLE Stock (
    IdStock NUMBER PRIMARY KEY,
    cantidad NUMBER,
    Productos NUMBER,
    FOREIGN KEY (Productos) REFERENCES Productos(IdProducto)
);

-- Tabla Pedidos
CREATE TABLE Pedidos (
    IdPedido NUMBER PRIMARY KEY,
    total NUMBER,
    fecha DATE,
    descripcion VARCHAR2(100),
    IdProveedor NUMBER,
    FOREIGN KEY (IdProveedor) REFERENCES Proveedores(IdProveedor)
);

-- Tabla detallePedido
CREATE TABLE detallePedido (
    IdDetalle NUMBER PRIMARY KEY,
    precioUni NUMBER,
    cantidad NUMBER,
    IdProducto NUMBER,
    FOREIGN KEY (IdProducto) REFERENCES Productos(IdProducto)
);

-- Tabla Factura
CREATE TABLE Factura (
    IdFactura NUMBER PRIMARY KEY,
    fecha DATE,
    total NUMBER,
    descripcion VARCHAR2(100),
    IdCliente NUMBER,
    FOREIGN KEY (IdCliente) REFERENCES Clientes(IdCliente)
);

-- Tabla detalleFactura
CREATE TABLE detalleFactura (
    IdDetalle NUMBER PRIMARY KEY,
    precioUni NUMBER,
    cantidad NUMBER,
    IdFactura NUMBER,
    IdProducto NUMBER,
    FOREIGN KEY (IdFactura) REFERENCES Factura(IdFactura),
    FOREIGN KEY (IdProducto) REFERENCES Productos(IdProducto)
);
